package com.game.model;

import com.badlogic.gdx.Preferences;

/** Persistent pearl wallet and equipment installations that continue while offline. */
public final class ProgressionStore {
    @FunctionalInterface
    public interface TimeSource {
        long currentTimeMillis();
    }

    private final Preferences preferences;
    private final TimeSource timeSource;

    public ProgressionStore(Preferences preferences) {
        this(preferences, System::currentTimeMillis);
    }

    public ProgressionStore(Preferences preferences, TimeSource timeSource) {
        this.preferences = preferences;
        this.timeSource = timeSource;
        SaveSchema.migrate(preferences);
    }

    public int pearls() {
        return Math.max(0, preferences.getInteger(SaveSchema.PEARLS_KEY, 0));
    }

    public int level(PermanentUpgrade upgrade) {
        completeInstallation(upgrade, currentTimeMillis());
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
        long now = currentTimeMillis();
        completeInstallation(upgrade, now);
        long readyAt = preferences.getLong(installationKey(upgrade), 0L);
        return readyAt == 0L ? 0L : Math.max(0L, readyAt - now);
    }

    public boolean purchase(PermanentUpgrade upgrade) {
        long now = currentTimeMillis();
        completeInstallation(upgrade, now);
        int level = installedLevel(upgrade);
        int cost = upgrade.costForLevel(level);
        if (level >= upgrade.maxLevel() || pearls() < cost
            || preferences.getLong(installationKey(upgrade), 0L) > now) {
            return false;
        }
        preferences.putInteger(SaveSchema.PEARLS_KEY, pearls() - cost);
        preferences.putLong(installationKey(upgrade),
            safeDeadline(now, upgrade.installDurationMillis(level + 1)));
        preferences.flush();
        return true;
    }

    /** Starts a new locally persisted reward transaction for a new dive or retry. */
    public long beginRun() {
        long sequence = Math.max(0L, preferences.getLong(SaveSchema.RUN_SEQUENCE_KEY, 0L));
        if (sequence == Long.MAX_VALUE) {
            sequence = 0L;
            preferences.putLong(SaveSchema.LAST_REWARDED_RUN_KEY, 0L);
        }
        sequence++;
        preferences.putLong(SaveSchema.RUN_SEQUENCE_KEY, sequence);
        preferences.flush();
        return sequence;
    }

    /** Awards one completion reward at most once for the supplied persisted run sequence. */
    public int awardRun(long runSequence, float meters, float runMultiplier,
                        EquipmentLoadout equipment) {
        long latestSequence = Math.max(0L,
            preferences.getLong(SaveSchema.RUN_SEQUENCE_KEY, 0L));
        long lastRewarded = Math.max(0L,
            preferences.getLong(SaveSchema.LAST_REWARDED_RUN_KEY, 0L));
        if (runSequence <= lastRewarded || runSequence <= 0L || runSequence != latestSequence) {
            return 0;
        }
        int reward = creditDistanceReward(meters, runMultiplier, equipment);
        preferences.putLong(SaveSchema.LAST_REWARDED_RUN_KEY, runSequence);
        preferences.flush();
        return reward;
    }

    public static int calculateDistanceReward(float meters, float runMultiplier,
                                               EquipmentLoadout equipment) {
        if (!Float.isFinite(meters) || meters < 100f || !Float.isFinite(runMultiplier)
            || runMultiplier <= 0f || equipment == null
            || !Float.isFinite(equipment.pearlRewardMultiplier())
            || equipment.pearlRewardMultiplier() <= 0f) {
            return 0;
        }
        int baseReward = Math.max(0, (int) (meters / 100f));
        float multiplier = runMultiplier * equipment.pearlRewardMultiplier();
        float reward = baseReward * multiplier;
        if (!Float.isFinite(reward) || reward >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, Math.round(reward));
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
        long now = currentTimeMillis();
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

    public boolean isSuitUnlocked(DiverSuit suit) {
        return suit == DiverSuit.TIDELINE_BLUE
            || preferences.getBoolean(suitKey(suit), false);
    }

    public DiverSuit selectedSuit() {
        String saved = preferences.getString(SaveSchema.SELECTED_SUIT_KEY,
            DiverSuit.TIDELINE_BLUE.name());
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
        preferences.putInteger(SaveSchema.PEARLS_KEY, pearls() - suit.cost());
        preferences.putBoolean(suitKey(suit), true);
        preferences.putString(SaveSchema.SELECTED_SUIT_KEY, suit.name());
        preferences.flush();
        return true;
    }

    public boolean selectSuit(DiverSuit suit) {
        if (!isSuitUnlocked(suit)) {
            return false;
        }
        preferences.putString(SaveSchema.SELECTED_SUIT_KEY, suit.name());
        preferences.flush();
        return true;
    }

    private static String levelKey(PermanentUpgrade upgrade) {
        return SaveSchema.levelKey(upgrade);
    }

    private static String installationKey(PermanentUpgrade upgrade) {
        return SaveSchema.installationKey(upgrade);
    }

    private static String suitKey(DiverSuit suit) {
        return SaveSchema.suitKey(suit);
    }

    private int creditDistanceReward(float meters, float runMultiplier,
                                     EquipmentLoadout equipment) {
        int calculated = calculateDistanceReward(meters, runMultiplier, equipment);
        int balance = pearls();
        int credited = Math.min(calculated, Integer.MAX_VALUE - balance);
        if (credited > 0) {
            preferences.putInteger(SaveSchema.PEARLS_KEY,
                SaveSchema.saturatingAdd(balance, credited));
        }
        return credited;
    }

    private long currentTimeMillis() {
        return Math.max(0L, timeSource.currentTimeMillis());
    }

    private static long safeDeadline(long now, long duration) {
        if (duration > Long.MAX_VALUE - now) {
            return Long.MAX_VALUE;
        }
        return now + duration;
    }
}
