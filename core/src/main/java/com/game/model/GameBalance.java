package com.game.model;

public final class GameBalance {
    public static final float BOSS_SCORE = 6500f;
    private static final Difficulty LEVEL_ONE = new Difficulty(1, 2f, 1f);
    private static final Difficulty LEVEL_TWO = new Difficulty(2, 1.7f, 1.2f);
    private static final Difficulty LEVEL_THREE = new Difficulty(3, 1.4f, 1.5f);
    private static final Difficulty LEVEL_FOUR = new Difficulty(4, 1.1f, 2f);

    private GameBalance() {
    }

    public static Difficulty difficultyFor(float score) {
        if (score >= 5000f) {
            return LEVEL_FOUR;
        }
        if (score >= 2500f) {
            return LEVEL_THREE;
        }
        if (score >= 1000f) {
            return LEVEL_TWO;
        }
        return LEVEL_ONE;
    }

    public static float comboMultiplier(int combo) {
        if (combo <= 1) {
            return 1f;
        }
        return Math.min(3f, 1f + (combo - 1) * 0.25f);
    }

    public static int killReward(int baseScore, int combo) {
        return Math.round(baseScore * comboMultiplier(combo));
    }

    public record Difficulty(int level, float spawnInterval, float enemySpeedMultiplier) {
    }
}
