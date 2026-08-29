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
    private final float maxOxygen;
    private final float upgradeEffectMultiplier;
    private final float oxygenDrainMultiplier;

    public GameSession() {
        this(MAX_OXYGEN, 1f, 1f);
    }

    public GameSession(float maxOxygen) {
        this(maxOxygen, 1f, 1f);
    }

    public GameSession(float maxOxygen, float upgradeEffectMultiplier,
                       float oxygenDrainMultiplier) {
        this.maxOxygen = Math.max(MAX_OXYGEN, maxOxygen);
        this.upgradeEffectMultiplier = Math.max(0f, upgradeEffectMultiplier);
        this.oxygenDrainMultiplier = Math.max(0f, oxygenDrainMultiplier);
        upgrades = new UpgradeLoadout();
        reset();
    }

    public void reset() {
        oxygen = maxOxygen;
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
            oxygen - OXYGEN_DRAIN_PER_SECOND * scaledMultiplier(
                upgrades.oxygenDrainMultiplier()) * oxygenDrainMultiplier * delta);
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
        oxygen = Math.min(maxOxygen, oxygen + KILL_OXYGEN_REWARD);
        return reward;
    }

    public void collectOxygen(float amount) {
        oxygen = Math.min(maxOxygen, oxygen + Math.max(0f, amount));
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
        return scaledMultiplier(upgrades.shootCooldownMultiplier());
    }

    public int getHarpoonHitCount() {
        int bonusHits = upgrades.harpoonHitCount() - 1;
        return 1 + Math.max(0, Math.round(bonusHits * upgradeEffectMultiplier));
    }

    public float getOxygenPickupBonus() {
        return upgrades.oxygenPickupBonus() * upgradeEffectMultiplier;
    }

    public float getAgilityMultiplier() {
        return scaledMultiplier(upgrades.agilityMultiplier());
    }

    public void addScore(float amount) {
        score += Math.max(0f, amount);
    }

    public float getOxygen() {
        return oxygen;
    }

    public float getOxygenRatio() {
        return oxygen / maxOxygen;
    }

    public float getMaxOxygen() {
        return maxOxygen;
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

    public boolean isOutOfOxygen() {
        return oxygen <= 0f;
    }

    private float scaledMultiplier(float multiplier) {
        return 1f + (multiplier - 1f) * upgradeEffectMultiplier;
    }
}
