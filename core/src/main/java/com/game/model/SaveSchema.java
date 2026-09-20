package com.game.model;

import com.badlogic.gdx.Preferences;

import java.util.Objects;

/** Ordered, idempotent migrations and narrow validation for the primary game preferences. */
public final class SaveSchema {
    public static final String VERSION_KEY = "save.schemaVersion";
    public static final int LEGACY_VERSION = 0;
    public static final int CURRENT_VERSION = 1;

    static final String PEARLS_KEY = "progression.pearls";
    static final String SELECTED_SUIT_KEY = "progression.suit.selected";
    static final String RUN_SEQUENCE_KEY = "progression.run.sequence";
    static final String LAST_REWARDED_RUN_KEY = "progression.run.lastRewarded";

    private static final String DIFFICULTY_KEY = "runSetup.difficulty";
    private static final String KILLS_KEY = "achievement.stats.kills";
    private static final String OXYGEN_KEY = "achievement.stats.oxygenPickups";
    private static final String POWER_UP_KEY = "achievement.stats.powerUps";
    private static final String HIGH_SCORE_KEY = "highScore";

    private SaveSchema() {
    }

    public static void migrate(Preferences preferences) {
        Objects.requireNonNull(preferences, "preferences");
        int storedVersion = preferences.getInteger(VERSION_KEY, LEGACY_VERSION);
        if (storedVersion > CURRENT_VERSION) {
            // A newer build owns this save. Do not rewrite fields using older assumptions.
            return;
        }

        int version = Math.max(LEGACY_VERSION, storedVersion);
        while (version < CURRENT_VERSION) {
            int nextVersion = version + 1;
            switch (nextVersion) {
                case 1 -> migrateLegacyToVersionOne(preferences);
                default -> throw new IllegalStateException("Missing save migration for version "
                    + nextVersion);
            }
            // Commit the marker only after the migration completed successfully.
            preferences.putInteger(VERSION_KEY, nextVersion);
            preferences.flush();
            version = nextVersion;
        }

        if (validateCurrentFields(preferences)) {
            preferences.flush();
        }
    }

    private static void migrateLegacyToVersionOne(Preferences preferences) {
        int pearls = nonNegative(preferences.getInteger(PEARLS_KEY, 0));
        boolean changed = pearls != preferences.getInteger(PEARLS_KEY, 0);

        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            String levelKey = levelKey(upgrade);
            int savedLevel = preferences.getInteger(levelKey, 0);
            if (savedLevel == 4 && upgrade != PermanentUpgrade.TWIN_LAUNCHER) {
                pearls = saturatingAdd(pearls, upgrade.costForLevel(3));
            }
            int validLevel = clamp(savedLevel, 0, upgrade.maxLevel());
            if (validLevel != savedLevel) {
                preferences.putInteger(levelKey, validLevel);
                changed = true;
            }
        }

        if (pearls != preferences.getInteger(PEARLS_KEY, 0)) {
            preferences.putInteger(PEARLS_KEY, pearls);
            changed = true;
        }
        if (validateCurrentFields(preferences)) {
            changed = true;
        }
        if (changed) {
            preferences.flush();
        }
    }

    private static boolean validateCurrentFields(Preferences preferences) {
        boolean changed = false;
        changed |= putIfDifferent(preferences, PEARLS_KEY,
            nonNegative(preferences.getInteger(PEARLS_KEY, 0)));
        changed |= putIfDifferent(preferences, HIGH_SCORE_KEY,
            nonNegative(preferences.getInteger(HIGH_SCORE_KEY, 0)));
        changed |= putIfDifferent(preferences, KILLS_KEY,
            nonNegative(preferences.getInteger(KILLS_KEY, 0)));
        changed |= putIfDifferent(preferences, OXYGEN_KEY,
            nonNegative(preferences.getInteger(OXYGEN_KEY, 0)));
        changed |= putIfDifferent(preferences, POWER_UP_KEY,
            nonNegative(preferences.getInteger(POWER_UP_KEY, 0)));

        long sequence = Math.max(0L, preferences.getLong(RUN_SEQUENCE_KEY, 0L));
        long lastRewarded = Math.max(0L, preferences.getLong(LAST_REWARDED_RUN_KEY, 0L));
        lastRewarded = Math.min(lastRewarded, sequence);
        changed |= putIfDifferent(preferences, RUN_SEQUENCE_KEY, sequence);
        changed |= putIfDifferent(preferences, LAST_REWARDED_RUN_KEY, lastRewarded);

        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            String levelKey = levelKey(upgrade);
            int savedLevel = preferences.getInteger(levelKey, 0);
            int validLevel = clamp(savedLevel, 0, upgrade.maxLevel());
            changed |= putIfDifferent(preferences, levelKey, validLevel);

            String installationKey = installationKey(upgrade);
            if (preferences.contains(installationKey)) {
                long readyAt = preferences.getLong(installationKey, 0L);
                if (readyAt <= 0L || validLevel >= upgrade.maxLevel()) {
                    preferences.remove(installationKey);
                    changed = true;
                }
            }
        }

        String selectedSuit = preferences.getString(SELECTED_SUIT_KEY,
            DiverSuit.TIDELINE_BLUE.name());
        DiverSuit validSuit = enumValue(DiverSuit.class, selectedSuit, DiverSuit.TIDELINE_BLUE);
        if (validSuit != DiverSuit.TIDELINE_BLUE
            && !preferences.getBoolean(suitKey(validSuit), false)) {
            validSuit = DiverSuit.TIDELINE_BLUE;
        }
        changed |= putIfDifferent(preferences, SELECTED_SUIT_KEY, validSuit.name());

        String difficulty = preferences.getString(DIFFICULTY_KEY, RunDifficulty.NORMAL.name());
        RunDifficulty validDifficulty = enumValue(RunDifficulty.class, difficulty,
            RunDifficulty.NORMAL);
        changed |= putIfDifferent(preferences, DIFFICULTY_KEY, validDifficulty.name());
        return changed;
    }

    static int saturatingAdd(int left, int right) {
        long sum = (long) left + right;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, sum));
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return fallback;
        }
    }

    private static boolean putIfDifferent(Preferences preferences, String key, int value) {
        Object stored = preferences.get().get(key);
        if (stored instanceof Integer integer && integer == value) {
            return false;
        }
        preferences.putInteger(key, value);
        return true;
    }

    private static boolean putIfDifferent(Preferences preferences, String key, long value) {
        Object stored = preferences.get().get(key);
        if (stored instanceof Long longValue && longValue == value) {
            return false;
        }
        preferences.putLong(key, value);
        return true;
    }

    private static boolean putIfDifferent(Preferences preferences, String key, String value) {
        Object stored = preferences.get().get(key);
        if (stored instanceof String string && value.equals(string)) {
            return false;
        }
        preferences.putString(key, value);
        return true;
    }

    static String levelKey(PermanentUpgrade upgrade) {
        return "progression.level." + upgrade.name();
    }

    static String installationKey(PermanentUpgrade upgrade) {
        return "progression.installation.readyAt." + upgrade.name();
    }

    static String suitKey(DiverSuit suit) {
        return "progression.suit.unlocked." + suit.name();
    }
}
