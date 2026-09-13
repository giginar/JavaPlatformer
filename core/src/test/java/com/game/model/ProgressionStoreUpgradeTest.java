package com.game.model;

import org.junit.jupiter.api.Test;

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
        assertEquals(100, store.awardDistance(10_000f, 1f, currentDive));
        assertEquals(115, store.awardDistance(10_000f, 1f, nextDive));
        assertEquals(100, store.awardDistance(10_000f, 1f, false));
    }

    @Test
    void existingPurchasesStayReadyAndRemovedFourthLevelsAreRefundedOnlyOnce() {
        preferences.putInteger("progression.pearls", 100);
        preferences.putInteger("progression.level.PRESSURE_TANK", 4);
        preferences.putInteger("progression.level.SALVAGE_MAP", 4);
        preferences.putInteger("progression.level.TWIN_LAUNCHER", 2);
        preferences.putBoolean("progression.suit.unlocked.RESCUE_RED", true);
        ProgressionStore migrated = new ProgressionStore(preferences, now::get);

        assertEquals(168, migrated.pearls());
        assertEquals(3, migrated.level(PermanentUpgrade.PRESSURE_TANK));
        assertEquals(3, migrated.level(PermanentUpgrade.SALVAGE_MAP));
        assertEquals(2, migrated.level(PermanentUpgrade.TWIN_LAUNCHER));
        assertEquals(0L, migrated.remainingInstallMillis(PermanentUpgrade.PRESSURE_TANK));
        assertTrue(migrated.isSuitUnlocked(DiverSuit.RESCUE_RED));
        assertEquals(168, new ProgressionStore(preferences, now::get).pearls());
        assertTrue(migrated.purchase(PermanentUpgrade.TWIN_LAUNCHER));
        assertEquals(900_000L, migrated.remainingInstallMillis(PermanentUpgrade.TWIN_LAUNCHER));
    }
}
