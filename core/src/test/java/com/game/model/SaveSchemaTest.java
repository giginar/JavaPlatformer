package com.game.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveSchemaTest {
    @Test
    void emptyLegacyPreferencesMigrateToCurrentSchema() {
        MemoryPreferences preferences = new MemoryPreferences();

        SaveSchema.migrate(preferences);

        assertEquals(SaveSchema.CURRENT_VERSION,
            preferences.getInteger(SaveSchema.VERSION_KEY));
        assertEquals(0, preferences.getInteger("progression.pearls"));
        assertEquals(DiverSuit.TIDELINE_BLUE.name(),
            preferences.getString("progression.suit.selected"));
        assertEquals(RunDifficulty.NORMAL.name(),
            preferences.getString("runSetup.difficulty"));
    }

    @Test
    void legacyMigrationPreservesAllLegitimateProgressionAndSettings() {
        MemoryPreferences preferences = populatedLegacySave();
        Map<String, ?> legacy = preferences.get();

        SaveSchema.migrate(preferences);

        assertEquals(77, preferences.getInteger("progression.pearls"));
        assertEquals(2, preferences.getInteger("progression.level.PRESSURE_TANK"));
        assertEquals(1_800_000_600_000L,
            preferences.getLong("progression.installation.readyAt.PRESSURE_TANK"));
        assertTrue(preferences.getBoolean("progression.suit.unlocked.RESCUE_RED"));
        assertEquals(DiverSuit.RESCUE_RED.name(),
            preferences.getString("progression.suit.selected"));
        assertTrue(preferences.getBoolean("achievement.unlocked.HUNTER_10"));
        assertEquals(42, preferences.getInteger("achievement.stats.kills"));
        assertEquals(8, preferences.getInteger("achievement.stats.oxygenPickups"));
        assertEquals(6, preferences.getInteger("achievement.stats.powerUps"));
        assertEquals(RunDifficulty.HARD.name(), preferences.getString("runSetup.difficulty"));
        assertTrue(preferences.getBoolean("runSetup.challenge.NO_WEAPON"));
        for (Map.Entry<String, ?> entry : legacy.entrySet()) {
            assertEquals(entry.getValue(), preferences.get().get(entry.getKey()), entry.getKey());
        }
    }

    @Test
    void migrationIsRepeatableAndLegacyRefundOccursOnlyOnce() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("progression.pearls", 100);
        preferences.putInteger("progression.level.PRESSURE_TANK", 4);
        preferences.putInteger("progression.level.SALVAGE_MAP", 4);

        SaveSchema.migrate(preferences);
        Map<String, ?> migrated = preferences.get();
        SaveSchema.migrate(preferences);

        assertEquals(168, preferences.getInteger("progression.pearls"));
        assertEquals(3, preferences.getInteger("progression.level.PRESSURE_TANK"));
        assertEquals(3, preferences.getInteger("progression.level.SALVAGE_MAP"));
        assertEquals(migrated, preferences.get());
    }

    @Test
    void corruptFieldsAreRepairedIndividuallyWithoutErasingValidData() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("progression.pearls", -20);
        preferences.putInteger("progression.level.PRESSURE_TANK", -7);
        preferences.putInteger("progression.level.REINFORCED_SUIT", 999);
        preferences.putLong("progression.installation.readyAt.PRESSURE_TANK", -1L);
        preferences.putString("progression.suit.selected", "UNKNOWN_SUIT");
        preferences.putBoolean("progression.suit.unlocked.SALVAGE_GREEN", true);
        preferences.putString("runSetup.difficulty", "IMPOSSIBLE");
        preferences.putInteger("achievement.stats.kills", -8);
        preferences.putInteger("achievement.stats.oxygenPickups", -9);
        preferences.putInteger("achievement.stats.powerUps", -10);
        preferences.putBoolean("achievement.unlocked.FIRST_BLOOD", true);
        preferences.putLong("progression.run.sequence", -4L);
        preferences.putLong("progression.run.lastRewarded", Long.MAX_VALUE);

        SaveSchema.migrate(preferences);

        assertEquals(0, preferences.getInteger("progression.pearls"));
        assertEquals(0, preferences.getInteger("progression.level.PRESSURE_TANK"));
        assertEquals(3, preferences.getInteger("progression.level.REINFORCED_SUIT"));
        assertFalse(preferences.contains("progression.installation.readyAt.PRESSURE_TANK"));
        assertEquals(DiverSuit.TIDELINE_BLUE.name(),
            preferences.getString("progression.suit.selected"));
        assertTrue(preferences.getBoolean("progression.suit.unlocked.SALVAGE_GREEN"));
        assertEquals(RunDifficulty.NORMAL.name(), preferences.getString("runSetup.difficulty"));
        assertEquals(0, preferences.getInteger("achievement.stats.kills"));
        assertEquals(0, preferences.getInteger("achievement.stats.oxygenPickups"));
        assertEquals(0, preferences.getInteger("achievement.stats.powerUps"));
        assertTrue(preferences.getBoolean("achievement.unlocked.FIRST_BLOOD"));
        assertEquals(0L, preferences.getLong("progression.run.sequence"));
        assertEquals(0L, preferences.getLong("progression.run.lastRewarded"));
    }

    @Test
    void unlockedSelectedSuitAndCompletedInstallationRemainStableAcrossRepeatedLoads() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("progression.level.MAGNETIC_CLASP", 1);
        preferences.putLong("progression.installation.readyAt.MAGNETIC_CLASP", 1_800_000_000_000L);
        preferences.putBoolean("progression.suit.unlocked.ABYSS_BLACK", true);
        preferences.putString("progression.suit.selected", DiverSuit.ABYSS_BLACK.name());

        SaveSchema.migrate(preferences);
        SaveSchema.migrate(preferences);

        assertEquals(1, preferences.getInteger("progression.level.MAGNETIC_CLASP"));
        assertEquals(1_800_000_000_000L,
            preferences.getLong("progression.installation.readyAt.MAGNETIC_CLASP"));
        assertEquals(DiverSuit.ABYSS_BLACK.name(),
            preferences.getString("progression.suit.selected"));
    }

    @Test
    void saveFromNewerSchemaIsNotRewrittenByOlderCode() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger(SaveSchema.VERSION_KEY, SaveSchema.CURRENT_VERSION + 1);
        preferences.putInteger("progression.pearls", -99);
        Map<String, ?> before = preferences.get();

        SaveSchema.migrate(preferences);

        assertEquals(before, preferences.get());
    }

    private static MemoryPreferences populatedLegacySave() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("progression.pearls", 77);
        preferences.putInteger("progression.level.PRESSURE_TANK", 2);
        preferences.putLong("progression.installation.readyAt.PRESSURE_TANK",
            1_800_000_600_000L);
        preferences.putBoolean("progression.suit.unlocked.RESCUE_RED", true);
        preferences.putString("progression.suit.selected", DiverSuit.RESCUE_RED.name());
        preferences.putBoolean("achievement.unlocked.HUNTER_10", true);
        preferences.putInteger("achievement.stats.kills", 42);
        preferences.putInteger("achievement.stats.oxygenPickups", 8);
        preferences.putInteger("achievement.stats.powerUps", 6);
        preferences.putString("runSetup.difficulty", RunDifficulty.HARD.name());
        preferences.putBoolean("runSetup.challenge.NO_WEAPON", true);
        return preferences;
    }
}
