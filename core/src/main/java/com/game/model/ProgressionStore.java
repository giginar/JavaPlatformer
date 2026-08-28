package com.game.model;

import com.badlogic.gdx.Preferences;

/** Persistent pearl wallet and permanent pre-dive equipment upgrades. */
public final class ProgressionStore {
    private static final String PEARLS_KEY = "progression.pearls";
    private final Preferences preferences;

    public ProgressionStore(Preferences preferences) {
        this.preferences = preferences;
    }

    public int pearls() {
        return preferences.getInteger(PEARLS_KEY, 0);
    }

    public int level(PermanentUpgrade upgrade) {
        return Math.max(0, Math.min(upgrade.maxLevel(),
            preferences.getInteger(levelKey(upgrade), 0)));
    }

    public int cost(PermanentUpgrade upgrade) {
        return upgrade.costForLevel(level(upgrade));
    }

    public boolean purchase(PermanentUpgrade upgrade) {
        int level = level(upgrade);
        int cost = upgrade.costForLevel(level);
        if (level >= upgrade.maxLevel() || pearls() < cost) {
            return false;
        }
        preferences.putInteger(PEARLS_KEY, pearls() - cost);
        preferences.putInteger(levelKey(upgrade), level + 1);
        preferences.flush();
        return true;
    }

    public int awardDistance(float meters) {
        int baseReward = Math.max(0, (int) (meters / 100f));
        float multiplier = 1f + level(PermanentUpgrade.SALVAGE_MAP) * 0.15f;
        int reward = Math.round(baseReward * multiplier);
        if (reward > 0) {
            preferences.putInteger(PEARLS_KEY, pearls() + reward);
            preferences.flush();
        }
        return reward;
    }

    public float startingMaxOxygen() {
        return GameSession.MAX_OXYGEN + level(PermanentUpgrade.PRESSURE_TANK) * 10f;
    }

    public float startingShieldSeconds() {
        return level(PermanentUpgrade.REINFORCED_SUIT) * 1.5f;
    }

    public float magnetBonusRange() {
        return level(PermanentUpgrade.MAGNETIC_CLASP) * 35f;
    }

    public int startingHarpoonBonus() {
        return level(PermanentUpgrade.TWIN_LAUNCHER);
    }

    private static String levelKey(PermanentUpgrade upgrade) {
        return "progression.level." + upgrade.name();
    }
}
