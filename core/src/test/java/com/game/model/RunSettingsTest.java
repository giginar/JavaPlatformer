package com.game.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunSettingsTest {
    @Test
    void standardRunUsesNormalDifficultyWithoutChallenges() {
        RunSettings settings = RunSettings.standard();

        assertEquals(RunDifficulty.NORMAL, settings.difficulty());
        assertFalse(settings.isChallengeRun());
        assertEquals(1f, settings.rewardMultiplier(), 0.0001f);
    }

    @Test
    void allChallengesAreDefensivelyCopiedAndIncreaseRewards() {
        EnumSet<ChallengeModifier> selected = EnumSet.allOf(ChallengeModifier.class);
        RunSettings settings = new RunSettings(RunDifficulty.HARD, selected);
        selected.clear();

        assertTrue(settings.hasAllChallenges());
        assertEquals(2.3f, settings.rewardMultiplier(), 0.0001f);
        assertThrows(UnsupportedOperationException.class,
            () -> settings.modifiers().clear());
    }

    @Test
    void hardDifficultyRaisesThreatAndReducesUpgradeEfficiency() {
        assertTrue(RunDifficulty.HARD.enemySpeedMultiplier()
            > RunDifficulty.NORMAL.enemySpeedMultiplier());
        assertTrue(RunDifficulty.HARD.damageMultiplier()
            > RunDifficulty.NORMAL.damageMultiplier());
        assertTrue(RunDifficulty.HARD.spawnIntervalMultiplier()
            < RunDifficulty.NORMAL.spawnIntervalMultiplier());
        assertTrue(RunDifficulty.HARD.upgradeEffectMultiplier()
            < RunDifficulty.NORMAL.upgradeEffectMultiplier());
    }

    @Test
    void selectedDifficultyCurvesRemainOrdered() {
        assertTrue(RunDifficulty.EASY.enemySpeedMultiplier()
            < RunDifficulty.NORMAL.enemySpeedMultiplier());
        assertTrue(RunDifficulty.NORMAL.enemySpeedMultiplier()
            < RunDifficulty.HARD.enemySpeedMultiplier());
        assertTrue(RunDifficulty.EASY.damageMultiplier()
            < RunDifficulty.NORMAL.damageMultiplier());
        assertTrue(RunDifficulty.NORMAL.damageMultiplier()
            < RunDifficulty.HARD.damageMultiplier());
        assertTrue(RunDifficulty.EASY.spawnIntervalMultiplier()
            > RunDifficulty.NORMAL.spawnIntervalMultiplier());
        assertTrue(RunDifficulty.NORMAL.spawnIntervalMultiplier()
            > RunDifficulty.HARD.spawnIntervalMultiplier());
        assertTrue(RunDifficulty.EASY.oxygenDrainMultiplier()
            < RunDifficulty.NORMAL.oxygenDrainMultiplier());
        assertTrue(RunDifficulty.NORMAL.oxygenDrainMultiplier()
            < RunDifficulty.HARD.oxygenDrainMultiplier());
        assertTrue(RunDifficulty.EASY.upgradeEffectMultiplier()
            > RunDifficulty.NORMAL.upgradeEffectMultiplier());
        assertTrue(RunDifficulty.NORMAL.upgradeEffectMultiplier()
            > RunDifficulty.HARD.upgradeEffectMultiplier());
        assertTrue(RunDifficulty.EASY.rewardMultiplier()
            < RunDifficulty.NORMAL.rewardMultiplier());
        assertTrue(RunDifficulty.NORMAL.rewardMultiplier()
            < RunDifficulty.HARD.rewardMultiplier());
    }

    @Test
    void disabledMechanicsAreExcludedFromRunUpgradeChoices() {
        RunSettings settings = new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_WEAPON,
                ChallengeModifier.NO_OXYGEN_PICKUPS));

        assertFalse(settings.isRunUpgradeUseful(UpgradeType.RAPID_FIRE));
        assertFalse(settings.isRunUpgradeUseful(UpgradeType.PIERCING_HARPOON));
        assertFalse(settings.isRunUpgradeUseful(UpgradeType.LARGE_TANKS));
        assertTrue(settings.isRunUpgradeUseful(UpgradeType.OXYGEN_EFFICIENCY));
        assertTrue(settings.isRunUpgradeUseful(UpgradeType.AGILE_DIVER));
    }

    @Test
    void noUpgradesDisablesEveryRunUpgradeChoice() {
        RunSettings settings = new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_UPGRADES));

        for (UpgradeType type : UpgradeType.values()) {
            assertFalse(settings.isRunUpgradeUseful(type));
        }
        assertFalse(settings.loadoutBonusesEnabled());
        assertEquals(1f, settings.suitAgilityMultiplier(DiverSuit.ABYSS_BLACK), 0.0001f);
    }

    @Test
    void eachChallengePolicyDisablesItsDocumentedRuntimeMechanic() {
        assertFalse(new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_WEAPON)).allowsWeapon());
        assertFalse(new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_OXYGEN_PICKUPS)).allowsOxygenPickups());
        assertFalse(new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_POWER_UPS)).allowsPowerUpPickups());
        assertFalse(new RunSettings(RunDifficulty.NORMAL,
            EnumSet.of(ChallengeModifier.NO_UPGRADES)).loadoutBonusesEnabled());
    }
}
