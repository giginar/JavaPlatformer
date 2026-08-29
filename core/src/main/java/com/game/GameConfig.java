package com.game;

public final class GameConfig {
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;
    public static final String PREFERENCES_NAME = "DeepDiveDriftPrefs";
    public static final float DEFAULT_STAGE_DURATION_SECONDS = 180f;
    public static final float MIN_STAGE_DURATION_SECONDS = 5f;
    public static final float MAX_STAGE_DURATION_SECONDS = 600f;
    public static final float DIVER_REFERENCE_HEIGHT_METERS = 1.8f;
    public static final float DIVER_REFERENCE_HEIGHT_WORLD_UNITS = 64f;
    public static final int SALVAGE_GREEN_SUIT_COST = 20;
    public static final int RESCUE_RED_SUIT_COST = 45;
    public static final int ABYSS_BLACK_SUIT_COST = 80;
    public static final float TIDELINE_BLUE_RELOAD_MULTIPLIER = 0.9f;
    public static final float SALVAGE_GREEN_MAGNET_BONUS = 90f;
    public static final float RESCUE_RED_OXYGEN_BONUS = 25f;
    public static final float ABYSS_BLACK_AGILITY_MULTIPLIER = 1.15f;
    public static final float EASY_ENEMY_SPEED_MULTIPLIER = 0.85f;
    public static final float EASY_DAMAGE_MULTIPLIER = 0.75f;
    public static final float EASY_SPAWN_INTERVAL_MULTIPLIER = 1.2f;
    public static final float EASY_UPGRADE_EFFECT_MULTIPLIER = 1.15f;
    public static final float EASY_OXYGEN_DRAIN_MULTIPLIER = 0.85f;
    public static final float EASY_REWARD_MULTIPLIER = 0.8f;
    public static final float HARD_ENEMY_SPEED_MULTIPLIER = 1.22f;
    public static final float HARD_DAMAGE_MULTIPLIER = 1.3f;
    public static final float HARD_SPAWN_INTERVAL_MULTIPLIER = 0.78f;
    public static final float HARD_UPGRADE_EFFECT_MULTIPLIER = 0.75f;
    public static final float HARD_OXYGEN_DRAIN_MULTIPLIER = 1.12f;
    public static final float HARD_REWARD_MULTIPLIER = 1.5f;
    public static final float CHALLENGE_REWARD_BONUS_PER_MODIFIER = 0.2f;
    public static final float UNARMED_BOSS_SURVIVAL_SECONDS = 30f;
    /** Slow descent keeps falling debris visible long enough to become a dodgeable threat. */
    public static final float FALLING_DEBRIS_ACCELERATION = 35f;
    public static final float FALLING_DEBRIS_MAX_SPEED = 70f;
    /** Keep enabled while testing; disable before creating a release build. */
    public static final boolean TEST_SHORTCUTS_ENABLED = false;

    /**
     * Runtime override used by short balance-test runs. Desktop accepts
     * {@code --stage-duration=15}; other launchers can set the same system property.
     */
    public static float stageDurationSeconds() {
        String value = System.getProperty("deepdive.stageDurationSeconds");
        if (value == null || value.isBlank()) {
            return DEFAULT_STAGE_DURATION_SECONDS;
        }
        try {
            float parsed = Float.parseFloat(value);
            if (!Float.isFinite(parsed)) {
                return DEFAULT_STAGE_DURATION_SECONDS;
            }
            return Math.max(MIN_STAGE_DURATION_SECONDS,
                Math.min(MAX_STAGE_DURATION_SECONDS, parsed));
        } catch (NumberFormatException ignored) {
            return DEFAULT_STAGE_DURATION_SECONDS;
        }
    }

    private GameConfig() {
    }
}
