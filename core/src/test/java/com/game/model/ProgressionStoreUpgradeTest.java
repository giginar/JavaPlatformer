package com.game.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionStoreUpgradeTest {
    private final MemoryPreferences preferences = new MemoryPreferences();
    private final AtomicLong now = new AtomicLong(1_800_000_000_000L);
    private final ProgressionStore store = new ProgressionStore(preferences, now::get);

    @Test
    void purchaseChargesOnceAndOnlyActivatesAtTheExactDeadline() {
        preferences.putInteger("progression.pearls", 100);

        assertTrue(store.purchase(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(92, store.pearls());
        assertEquals(0, store.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(300_000L, store.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(100f, store.startingMaxOxygen());
        assertFalse(store.purchase(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(92, store.pearls());

        now.addAndGet(299_999L);
        assertEquals(0, store.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(1L, store.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        now.incrementAndGet();
        assertEquals(1, store.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(0L, store.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(110f, store.startingMaxOxygen());
        assertEquals(1, new ProgressionStore(preferences, now::get).level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(92, store.pearls());
    }

    @Test
    void allUpgradesInstallThreeSequentialLevelsAndRejectAFourthPurchase() {
        preferences.putInteger("progression.pearls", 1000);
        int expectedPearls = 1000;
        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            for (int targetLevel = 1; targetLevel <= 3; targetLevel++) {
                expectedPearls -= upgrade.costForLevel(targetLevel - 1);
                assertTrue(store.purchase(upgrade), upgrade.name());
                assertEquals(expectedPearls, store.pearls());
                assertEquals(targetLevel * 300_000L, store.remainingInstallMillis(upgrade));
                assertEquals(targetLevel - 1, store.level(upgrade));
                assertFalse(store.purchase(upgrade));
                now.addAndGet(targetLevel * 300_000L);
                assertEquals(targetLevel, store.level(upgrade));
            }
            assertFalse(store.purchase(upgrade));
            assertEquals(expectedPearls, store.pearls());
            assertEquals(0L, store.remainingInstallMillis(upgrade));
        }
        EquipmentLoadout equipment = store.snapshotEquipment();
        assertEquals(130f, equipment.startingMaxOxygen());
        assertEquals(4.5f, equipment.startingShieldSeconds());
        assertEquals(105f, equipment.magnetBonusRange());
        assertEquals(3, equipment.startingHarpoonBonus());
        assertEquals(1.45f, equipment.pearlRewardMultiplier(), 0.0001f);
    }

    @Test
    void insufficientPearlsNeverStartAnInstallation() {
        preferences.putInteger("progression.pearls", 7);

        assertFalse(store.purchase(PermanentUpgrade.PRESSURE_TANK));
        now.addAndGet(900_000L);
        assertEquals(7, store.pearls());
        assertEquals(0, store.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(0L, store.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
    }

    @Test
    void separateEquipmentCanInstallConcurrently() {
        preferences.putInteger("progression.pearls", 50);
        assertTrue(store.purchase(PermanentUpgrade.PRESSURE_TANK));
        now.addAndGet(60_000L);
        assertTrue(store.purchase(PermanentUpgrade.TWIN_LAUNCHER));

        now.addAndGet(240_000L);
        assertEquals(1, store.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(0, store.level(PermanentUpgrade.TWIN_LAUNCHER));
        assertEquals(60_000L, store.remainingInstallMillis(PermanentUpgrade.TWIN_LAUNCHER));
        assertEquals(28, store.pearls());
    }

    @Test
    void reopeningTheGamePreservesTheDeadlineAndOfflineTimeCompletesOnlyThePurchasedLevel() {
        preferences.putInteger("progression.pearls", 100);
        assertTrue(store.purchase(PermanentUpgrade.PRESSURE_TANK));
        now.addAndGet(120_000L);
        MemoryPreferences reloadedPreferences = new MemoryPreferences();
        reloadedPreferences.put(preferences.get());
        ProgressionStore reopened = new ProgressionStore(reloadedPreferences, now::get);
        assertEquals(180_000L, reopened.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertFalse(reopened.purchase(PermanentUpgrade.PRESSURE_TANK));

        now.addAndGet(86_400_000L);
        assertEquals(1, reopened.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(110f, reopened.startingMaxOxygen());
        assertEquals(92, reopened.pearls());
        assertEquals(0L, reopened.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertTrue(reopened.purchase(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(600_000L, reopened.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(110f, reopened.startingMaxOxygen());
    }

    @Test
    void completedInstallationsOnlyAffectTheNextDiveIncludingItsPearlReward() {
        preferences.putInteger("progression.pearls", 1000);
        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            assertTrue(store.purchase(upgrade));
        }
        EquipmentLoadout currentDive = store.snapshotEquipment();
        GameSession currentSession = new GameSession(currentDive.startingMaxOxygen());
        now.addAndGet(300_000L);
        EquipmentLoadout nextDive = store.snapshotEquipment();

        assertEquals(EquipmentLoadout.NONE, currentDive);
        assertEquals(100f, currentSession.getMaxOxygen());
        assertEquals(110f, new GameSession(nextDive.startingMaxOxygen()).getMaxOxygen());
        assertEquals(1.5f, nextDive.startingShieldSeconds());
        assertEquals(35f, nextDive.magnetBonusRange());
        assertEquals(1, nextDive.startingHarpoonBonus());
        assertEquals(100, ProgressionStore.calculateDistanceReward(10_000f, 1f, currentDive));
        assertEquals(115, ProgressionStore.calculateDistanceReward(10_000f, 1f, nextDive));
        assertEquals(100, ProgressionStore.calculateDistanceReward(
            10_000f, 1f, EquipmentLoadout.NONE));
    }

    @Test
    void existingPurchasesStayReadyAndRemovedFourthLevelsAreRefundedOnlyOnce() {
        MemoryPreferences legacyPreferences = new MemoryPreferences();
        legacyPreferences.putInteger("progression.pearls", 100);
        legacyPreferences.putInteger("progression.level.PRESSURE_TANK", 4);
        legacyPreferences.putInteger("progression.level.SALVAGE_MAP", 4);
        legacyPreferences.putInteger("progression.level.TWIN_LAUNCHER", 2);
        legacyPreferences.putBoolean("progression.suit.unlocked.RESCUE_RED", true);
        ProgressionStore migrated = new ProgressionStore(legacyPreferences, now::get);

        assertEquals(168, migrated.pearls());
        assertEquals(3, migrated.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(3, migrated.level(PermanentUpgrade.SALVAGE_MAP));
        assertEquals(2, migrated.level(PermanentUpgrade.TWIN_LAUNCHER));
        assertEquals(0L, migrated.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertTrue(migrated.isSuitUnlocked(DiverSuit.RESCUE_RED));
        assertEquals(168, new ProgressionStore(legacyPreferences, now::get).pearls());
        assertTrue(migrated.purchase(PermanentUpgrade.TWIN_LAUNCHER));
        assertEquals(900_000L, migrated.remainingInstallMillis(PermanentUpgrade.TWIN_LAUNCHER));
    }

    @Test
    void persistedRunSequenceMakesCompletionRewardsIdempotentAcrossReloads() {
        long run = store.beginRun();

        assertEquals(25, store.awardRun(run, 2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(25, store.pearls());
        assertEquals(0, store.awardRun(run, 2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(0, new ProgressionStore(preferences, now::get)
            .awardRun(run, 2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(25, store.pearls());

        long retry = store.beginRun();
        assertEquals(25, store.awardRun(retry, 2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(50, store.pearls());
    }

    @Test
    void abandonedRunCannotRewardAfterANewerRunOrRetryStarts() {
        long abandonedRun = store.beginRun();
        long currentRun = store.beginRun();

        assertEquals(0, store.awardRun(abandonedRun, 10_000f, 1f, EquipmentLoadout.NONE));
        assertEquals(100, store.awardRun(currentRun, 10_000f, 1f, EquipmentLoadout.NONE));
        assertEquals(100, store.pearls());
    }

    @Test
    void processDeathLeavesAnAbandonedRunUnrewardedAndTheNextRunInvalidatesIt() {
        long abandonedRun = store.beginRun();

        ProgressionStore relaunched = new ProgressionStore(preferences, now::get);
        assertEquals(0, relaunched.pearls());
        long nextRun = relaunched.beginRun();

        assertEquals(0, relaunched.awardRun(abandonedRun, 10_000f, 1f,
            EquipmentLoadout.NONE));
        assertEquals(100, relaunched.awardRun(nextRun, 10_000f, 1f,
            EquipmentLoadout.NONE));
        assertEquals(100, relaunched.pearls());
    }

    @Test
    void rewardCalculationFloorsDistanceThenRoundsCombinedMultipliersOnce() {
        EquipmentLoadout salvageLevelOne = new EquipmentLoadout(100f, 0f, 0f, 0, 1.15f);

        assertEquals(49, ProgressionStore.calculateDistanceReward(
            2_599f, 1.7f, salvageLevelOne));
        assertEquals(25, ProgressionStore.calculateDistanceReward(
            2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(0, ProgressionStore.calculateDistanceReward(
            99.999f, 2f, EquipmentLoadout.NONE));
    }

    @Test
    void representativeDifficultyChallengeAndSalvageRewardsMatchTheDisplayedFormula() {
        float completedRunDistance = 2_580.255f;
        RunSettings normalOneChallenge = new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_POWER_UPS));
        RunSettings hardAllChallenges = new RunSettings(RunDifficulty.HARD,
            EnumSet.allOf(ChallengeModifier.class));
        EquipmentLoadout maxSalvageMap = new EquipmentLoadout(100f, 0f, 0f, 0, 1.45f);

        assertEquals(20, ProgressionStore.calculateDistanceReward(completedRunDistance,
            RunDifficulty.EASY.rewardMultiplier(), EquipmentLoadout.NONE));
        assertEquals(25, ProgressionStore.calculateDistanceReward(completedRunDistance,
            RunDifficulty.NORMAL.rewardMultiplier(), EquipmentLoadout.NONE));
        assertEquals(38, ProgressionStore.calculateDistanceReward(completedRunDistance,
            RunDifficulty.HARD.rewardMultiplier(), EquipmentLoadout.NONE));
        assertEquals(30, ProgressionStore.calculateDistanceReward(completedRunDistance,
            normalOneChallenge.rewardMultiplier(), EquipmentLoadout.NONE));
        assertEquals(58, ProgressionStore.calculateDistanceReward(completedRunDistance,
            hardAllChallenges.rewardMultiplier(), EquipmentLoadout.NONE));
        assertEquals(36, ProgressionStore.calculateDistanceReward(completedRunDistance,
            RunDifficulty.NORMAL.rewardMultiplier(), maxSalvageMap));
        assertEquals(83, ProgressionStore.calculateDistanceReward(completedRunDistance,
            hardAllChallenges.rewardMultiplier(), maxSalvageMap));
    }

    @Test
    void extremeRewardsSaturateTheWalletWithoutOverflow() {
        preferences.putInteger("progression.pearls", Integer.MAX_VALUE - 2);
        long run = store.beginRun();

        assertEquals(2, store.awardRun(run, Float.MAX_VALUE, 1f, EquipmentLoadout.NONE));
        assertEquals(Integer.MAX_VALUE, store.pearls());
        long nextRun = store.beginRun();
        assertEquals(0, store.awardRun(nextRun, 10_000f, 1f, EquipmentLoadout.NONE));
        assertEquals(Integer.MAX_VALUE, store.pearls());
        assertEquals(0, ProgressionStore.calculateDistanceReward(
            Float.NaN, 1f, EquipmentLoadout.NONE));
        assertEquals(0, ProgressionStore.calculateDistanceReward(
            10_000f, Float.POSITIVE_INFINITY, EquipmentLoadout.NONE));
    }
}
