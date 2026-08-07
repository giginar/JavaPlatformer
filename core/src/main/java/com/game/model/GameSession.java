package com.game.model;

public final class GameSession {
    public static final float MAX_OXYGEN = 100f;
    public static final float OXYGEN_DRAIN_PER_SECOND = 5f;
    public static final float SURVIVAL_SCORE_PER_SECOND = 60f;
    public static final float COMBO_WINDOW = 3f;
    public static final float KILL_OXYGEN_REWARD = 8f;

    private float oxygen;
    private float score;
    private int combo;
    private float comboTimer;
    private final UpgradeLoadout upgrades;

    public GameSession() {
        upgrades = new UpgradeLoadout();
        reset();
    }

    public void reset() {
        oxygen = MAX_OXYGEN;
        score = 0f;
        combo = 0;
        comboTimer = 0f;
        upgrades.reset();
    }

    public void update(float delta) {
        if (delta <= 0f) {
            return;
        }

        oxygen = Math.max(0f,
            oxygen - OXYGEN_DRAIN_PER_SECOND * upgrades.oxygenDrainMultiplier() * delta);
        score += SURVIVAL_SCORE_PER_SECOND * delta;

        if (comboTimer > 0f) {
            comboTimer = Math.max(0f, comboTimer - delta);
            if (comboTimer == 0f) {
                combo = 0;
            }
        }
    }

    public int awardKill(int baseScore) {
        combo++;
        comboTimer = COMBO_WINDOW;
        int reward = GameBalance.killReward(baseScore, combo);
        score += reward;
        oxygen = Math.min(MAX_OXYGEN, oxygen + KILL_OXYGEN_REWARD);
        return reward;
    }

    public void collectOxygen(float amount) {
        oxygen = Math.min(MAX_OXYGEN, oxygen + Math.max(0f, amount));
    }

    public void takeDamage(float amount) {
        oxygen = Math.max(0f, oxygen - Math.max(0f, amount));
        combo = 0;
        comboTimer = 0f;
    }

    public boolean applyUpgrade(UpgradeType type) {
        return upgrades.apply(type);
    }

    public int getUpgradeLevel(UpgradeType type) {
        return upgrades.level(type);
    }

    public java.util.List<UpgradeType> getAvailableUpgrades() {
        return upgrades.availableUpgrades();
    }

    public float getShootCooldownMultiplier() {
        return upgrades.shootCooldownMultiplier();
    }

    public int getHarpoonHitCount() {
        return upgrades.harpoonHitCount();
    }

    public float getOxygenPickupBonus() {
        return upgrades.oxygenPickupBonus();
    }

    public float getAgilityMultiplier() {
        return upgrades.agilityMultiplier();
    }

    public void addScore(float amount) {
        score += Math.max(0f, amount);
    }

    public float getOxygen() {
        return oxygen;
    }

    public float getOxygenRatio() {
        return oxygen / MAX_OXYGEN;
    }

    public float getScore() {
        return score;
    }

    public int getDisplayScore() {
        return (int) score;
    }

    public int getCombo() {
        return combo;
    }

    public float getComboMultiplier() {
        return GameBalance.comboMultiplier(combo);
    }

    public GameBalance.Difficulty getDifficulty() {
        return GameBalance.difficultyFor(score);
    }

    public boolean isOutOfOxygen() {
        return oxygen <= 0f;
    }
}
