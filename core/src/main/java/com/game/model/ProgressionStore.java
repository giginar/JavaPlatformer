package com.game.model;

import com.badlogic.gdx.Preferences;

/** Persistent pearl wallet and equipment installations that continue while offline. */
public final class ProgressionStore {
    @FunctionalInterface
    public interface TimeSource {
        long currentTimeMillis();
    }

    private static final String PEARLS_KEY = "progression.pearls";
    private static final String SELECTED_SUIT_KEY = "progression.suit.selected";
    private final Preferences preferences;
    private final TimeSource timeSource;

    public ProgressionStore(Preferences preferences) {
        this(preferences, System::currentTimeMillis);
    }

    public ProgressionStore(Preferences preferences, TimeSource timeSource) {
        this.preferences = preferences;
        this.timeSource = timeSource;
        migrateLegacyLevels();
    }

    public int pearls() {
        return preferences.getInteger(PEARLS_KEY, 0);
    }

    public int level(PermanentUpgrade upgrade) {
        completeInstallation(upgrade, timeSource.currentTimeMillis());
        return installedLevel(upgrade);
    }

    private int installedLevel(PermanentUpgrade upgrade) {
        return Math.max(0, Math.min(upgrade.maxLevel(),
            preferences.getInteger(levelKey(upgrade), 0)));
    }

    public int cost(PermanentUpgrade upgrade) {
        return upgrade.costForLevel(level(upgrade));
    }

    public long remainingInstallMillis(PermanentUpgrade upgrade) {
        long now = timeSource.currentTimeMillis();
        completeInstallation(upgrade, now);
        long readyAt = preferences.getLong(installationKey(upgrade), 0L);
        return readyAt == 0L ? 0L : Math.max(0L, readyAt - now);
    }

    public boolean purchase(PermanentUpgrade upgrade) {
        long now = timeSource.currentTimeMillis();
        completeInstallation(upgrade, now);
        int level = installedLevel(upgrade);
        int cost = upgrade.costForLevel(level);
        if (level >= upgrade.maxLevel() || pearls() < cost
            || preferences.getLong(installationKey(upgrade), 0L) > now) {
            return false;
        }
        preferences.putInteger(PEARLS_KEY, pearls() - cost);
        preferences.putLong(installationKey(upgrade), now + upgrade.installDurationMillis(level + 1));
        preferences.flush();
        return true;
    }

    public int awardDistance(float meters) {
        return awardDistance(meters, 1f, true);
    }

    public int awardDistance(float meters, float runMultiplier, boolean permanentBonusesEnabled) {
        return awardDistance(meters, runMultiplier,
            permanentBonusesEnabled ? snapshotEquipment() : EquipmentLoadout.NONE);
    }

    public int awardDistance(float meters, float runMultiplier, EquipmentLoadout equipment) {
        int baseReward = Math.max(0, (int) (meters / 100f));
        float multiplier = Math.max(0f, runMultiplier) * equipment.pearlRewardMultiplier();
        int reward = Math.round(baseReward * multiplier);
        if (reward > 0) {
            preferences.putInteger(PEARLS_KEY, pearls() + reward);
            preferences.flush();
        }
        return reward;
    }

    public float startingMaxOxygen() {
        return snapshotEquipment().startingMaxOxygen();
    }

    public float startingShieldSeconds() {
        return snapshotEquipment().startingShieldSeconds();
    }

    public float magnetBonusRange() {
        return snapshotEquipment().magnetBonusRange();
    }

    public int startingHarpoonBonus() {
        return snapshotEquipment().startingHarpoonBonus();
    }

    public EquipmentLoadout snapshotEquipment() {
        long now = timeSource.currentTimeMillis();
        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            completeInstallation(upgrade, now);
        }
        return new EquipmentLoadout(
            GameSession.MAX_OXYGEN + installedLevel(PermanentUpgrade.PRESSURE_TANK) * 10f,
            installedLevel(PermanentUpgrade.REINFORCED_SUIT) * 1.5f,
            installedLevel(PermanentUpgrade.MAGNETIC_CLASP) * 35f,
            installedLevel(PermanentUpgrade.TWIN_LAUNCHER),
            1f + installedLevel(PermanentUpgrade.SALVAGE_MAP) * 0.15f);
    }

    private void completeInstallation(PermanentUpgrade upgrade, long now) {
        long readyAt = preferences.getLong(installationKey(upgrade), 0L);
        if (readyAt > 0L && readyAt <= now) {
            preferences.putInteger(levelKey(upgrade),
                Math.min(upgrade.maxLevel(), installedLevel(upgrade) + 1));
            preferences.remove(installationKey(upgrade));
            preferences.flush();
        }
    }

    private void migrateLegacyLevels() {
        boolean changed = false;
        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            int savedLevel = preferences.getInteger(levelKey(upgrade), 0);
            if (savedLevel > upgrade.maxLevel()) {
                // The previous shop sold four levels (two for Twin Launcher).
                if (savedLevel == 4 && upgrade != PermanentUpgrade.TWIN_LAUNCHER) {
                    preferences.putInteger(PEARLS_KEY, pearls() + upgrade.costForLevel(3));
                }
                preferences.putInteger(levelKey(upgrade), upgrade.maxLevel());
                changed = true;
            }
        }
        if (changed) {
            preferences.flush();
        }
    }

    public boolean isSuitUnlocked(DiverSuit suit) {
        return suit == DiverSuit.TIDELINE_BLUE
            || preferences.getBoolean(suitKey(suit), false);
    }

    public DiverSuit selectedSuit() {
        String saved = preferences.getString(SELECTED_SUIT_KEY, DiverSuit.TIDELINE_BLUE.name());
        try {
            DiverSuit suit = DiverSuit.valueOf(saved);
            return isSuitUnlocked(suit) ? suit : DiverSuit.TIDELINE_BLUE;
        } catch (IllegalArgumentException ignored) {
            return DiverSuit.TIDELINE_BLUE;
        }
    }

    public boolean purchaseSuit(DiverSuit suit) {
        if (isSuitUnlocked(suit) || pearls() < suit.cost()) {
            return false;
        }
        preferences.putInteger(PEARLS_KEY, pearls() - suit.cost());
        preferences.putBoolean(suitKey(suit), true);
        preferences.putString(SELECTED_SUIT_KEY, suit.name());
        preferences.flush();
        return true;
    }

    public boolean selectSuit(DiverSuit suit) {
        if (!isSuitUnlocked(suit)) {
            return false;
        }
        preferences.putString(SELECTED_SUIT_KEY, suit.name());
        preferences.flush();
        return true;
    }

    private static String levelKey(PermanentUpgrade upgrade) {
        return "progression.level." + upgrade.name();
    }

    private static String installationKey(PermanentUpgrade upgrade) {
        return "progression.installation.readyAt." + upgrade.name();
    }

    private static String suitKey(DiverSuit suit) {
        return "progression.suit.unlocked." + suit.name();
    }
}
