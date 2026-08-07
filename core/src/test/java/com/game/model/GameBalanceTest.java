package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameBalanceTest {
    @Test
    void difficultyChangesAtExactScoreThresholds() {
        assertEquals(1, GameBalance.difficultyFor(999f).level());
        assertEquals(2, GameBalance.difficultyFor(1000f).level());
        assertEquals(3, GameBalance.difficultyFor(2500f).level());
        assertEquals(4, GameBalance.difficultyFor(5000f).level());
    }

    @Test
    void difficultyCarriesSpawnAndSpeedSettings() {
        GameBalance.Difficulty levelFour = GameBalance.difficultyFor(5000f);

        assertEquals(1.1f, levelFour.spawnInterval(), 0.0001f);
        assertEquals(2f, levelFour.enemySpeedMultiplier(), 0.0001f);
    }

    @Test
    void comboRewardGrowsAndCapsAtTripleValue() {
        assertEquals(100, GameBalance.killReward(100, 1));
        assertEquals(125, GameBalance.killReward(100, 2));
        assertEquals(300, GameBalance.killReward(100, 20));
    }
}
