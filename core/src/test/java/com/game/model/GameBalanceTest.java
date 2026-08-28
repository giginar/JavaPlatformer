package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameBalanceTest {
    @Test
    void difficultyProvidesFiveAuthoredStages() {
        assertEquals(5, GameBalance.stageCount());
        assertEquals("SUNLIT REEF", GameBalance.difficultyForStage(1).name());
        assertEquals("ABYSSAL RIFT", GameBalance.difficultyForStage(5).name());
    }

    @Test
    void difficultyCarriesSpawnAndSpeedSettings() {
        GameBalance.Difficulty levelFour = GameBalance.difficultyForStage(4);

        assertEquals(1.3f, levelFour.spawnInterval(), 0.0001f);
        assertEquals(1.25f, levelFour.enemySpeedMultiplier(), 0.0001f);
        assertEquals(3.35f, levelFour.hazardInterval(), 0.0001f);
    }

    @Test
    void comboRewardGrowsAndCapsAtTripleValue() {
        assertEquals(100, GameBalance.killReward(100, 1));
        assertEquals(125, GameBalance.killReward(100, 2));
        assertEquals(300, GameBalance.killReward(100, 20));
    }

    @Test
    void anyPositiveDamageConsumesAllRemainingOxygenInFinalStage() {
        assertEquals(18f, GameBalance.playerDamageForStage(4, 18f, 120f), 0.0001f);
        assertEquals(120f, GameBalance.playerDamageForStage(5, 1f, 120f), 0.0001f);
        assertEquals(0f, GameBalance.playerDamageForStage(5, 0f, 120f), 0.0001f);
    }

    @Test
    void harpoonFireRateHasAReadableFloorAndActiveProjectileLimit() {
        assertEquals(0.32f, GameBalance.harpoonCooldown(1f, false), 0.0001f);
        assertEquals(0.1984f, GameBalance.harpoonCooldown(1f, true), 0.0001f);
        assertEquals(0.16f, GameBalance.harpoonCooldown(0.46f, true), 0.0001f);
        assertEquals(5, GameBalance.maxActiveHarpoons(false));
        assertEquals(8, GameBalance.maxActiveHarpoons(true));
    }
}
