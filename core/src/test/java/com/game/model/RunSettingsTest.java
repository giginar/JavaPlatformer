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
}
