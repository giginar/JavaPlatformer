package com.game.model;

import com.game.GameConfig;

/** Player-selected difficulty layered on top of the five-stage depth curve. */
public enum RunDifficulty {
    EASY(
        "EASY",
        "Slower threats, softer hits, stronger upgrades",
        GameConfig.EASY_ENEMY_SPEED_MULTIPLIER,
        GameConfig.EASY_DAMAGE_MULTIPLIER,
        GameConfig.EASY_SPAWN_INTERVAL_MULTIPLIER,
        GameConfig.EASY_UPGRADE_EFFECT_MULTIPLIER,
        GameConfig.EASY_OXYGEN_DRAIN_MULTIPLIER,
        GameConfig.EASY_REWARD_MULTIPLIER
    ),
    NORMAL("NORMAL", "The intended Deep Dive Drift balance", 1f, 1f, 1f, 1f, 1f, 1f),
    HARD(
        "HARD",
        "Faster threats, heavier hits, weaker upgrades",
        GameConfig.HARD_ENEMY_SPEED_MULTIPLIER,
        GameConfig.HARD_DAMAGE_MULTIPLIER,
        GameConfig.HARD_SPAWN_INTERVAL_MULTIPLIER,
        GameConfig.HARD_UPGRADE_EFFECT_MULTIPLIER,
        GameConfig.HARD_OXYGEN_DRAIN_MULTIPLIER,
        GameConfig.HARD_REWARD_MULTIPLIER
    );

    private final String title;
    private final String description;
    private final float enemySpeedMultiplier;
    private final float damageMultiplier;
    private final float spawnIntervalMultiplier;
    private final float upgradeEffectMultiplier;
    private final float oxygenDrainMultiplier;
    private final float rewardMultiplier;

    RunDifficulty(String title, String description, float enemySpeedMultiplier,
                  float damageMultiplier, float spawnIntervalMultiplier,
                  float upgradeEffectMultiplier, float oxygenDrainMultiplier,
                  float rewardMultiplier) {
        this.title = title;
        this.description = description;
        this.enemySpeedMultiplier = enemySpeedMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.spawnIntervalMultiplier = spawnIntervalMultiplier;
        this.upgradeEffectMultiplier = upgradeEffectMultiplier;
        this.oxygenDrainMultiplier = oxygenDrainMultiplier;
        this.rewardMultiplier = rewardMultiplier;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public float enemySpeedMultiplier() {
        return enemySpeedMultiplier;
    }

    public float damageMultiplier() {
        return damageMultiplier;
    }

    public float spawnIntervalMultiplier() {
        return spawnIntervalMultiplier;
    }

    public float upgradeEffectMultiplier() {
        return upgradeEffectMultiplier;
    }

    public float oxygenDrainMultiplier() {
        return oxygenDrainMultiplier;
    }

    public float rewardMultiplier() {
        return rewardMultiplier;
    }
}
