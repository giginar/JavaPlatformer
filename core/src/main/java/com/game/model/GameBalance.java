package com.game.model;

public final class GameBalance {
    private static final float BASE_HARPOON_COOLDOWN = 0.32f;
    private static final float OVERDRIVE_COOLDOWN_MULTIPLIER = 0.62f;
    private static final float MIN_HARPOON_COOLDOWN = 0.16f;
    private static final int MAX_ACTIVE_HARPOONS = 5;
    private static final int MAX_ACTIVE_OVERDRIVE_HARPOONS = 8;

    private static final Difficulty[] STAGES = {
        new Difficulty(1, "SUNLIT REEF", 2.15f, 1f, 5.4f, 1f),
        new Difficulty(2, "SINKING RUINS", 1.85f, 1.08f, 4.7f, 1.05f),
        new Difficulty(3, "CURRENT MAZE", 1.55f, 1.16f, 4f, 1.1f),
        new Difficulty(4, "BLACKWATER TRENCH", 1.3f, 1.25f, 3.35f, 1.16f),
        new Difficulty(5, "ABYSSAL RIFT", 1.08f, 1.34f, 2.75f, 1.23f)
    };

    private GameBalance() {
    }

    public static Difficulty difficultyForStage(int stage) {
        int index = Math.max(0, Math.min(STAGES.length - 1, stage - 1));
        return STAGES[index];
    }

    public static int stageCount() {
        return STAGES.length;
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

    public static float harpoonCooldown(float upgradeMultiplier, boolean overdrive) {
        float powerMultiplier = overdrive ? OVERDRIVE_COOLDOWN_MULTIPLIER : 1f;
        return Math.max(MIN_HARPOON_COOLDOWN,
            BASE_HARPOON_COOLDOWN * Math.max(0f, upgradeMultiplier) * powerMultiplier);
    }

    public static int maxActiveHarpoons(boolean overdrive) {
        return overdrive ? MAX_ACTIVE_OVERDRIVE_HARPOONS : MAX_ACTIVE_HARPOONS;
    }

    public static float playerDamageForStage(int stage, float incomingDamage,
                                             float currentOxygen) {
        float damage = Math.max(0f, incomingDamage);
        if (stage >= stageCount() && damage > 0f) {
            return Math.max(0f, currentOxygen);
        }
        return damage;
    }

    public record Difficulty(int level, String name, float spawnInterval,
                             float enemySpeedMultiplier, float hazardInterval,
                             float scrollSpeedMultiplier) {
    }
}
