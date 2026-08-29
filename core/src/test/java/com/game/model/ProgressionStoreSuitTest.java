package com.game.model;

import com.badlogic.gdx.Preferences;
import com.game.GameConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionStoreSuitTest {
    @Test
    void blueSuitIsTheUnlockedDefault() {
        ProgressionStore store = new ProgressionStore(new MemoryPreferences());

        assertTrue(store.isSuitUnlocked(DiverSuit.TIDELINE_BLUE));
        assertEquals(DiverSuit.TIDELINE_BLUE, store.selectedSuit());
        assertFalse(store.isSuitUnlocked(DiverSuit.RESCUE_RED));
    }

    @Test
    void purchasingSuitDeductsPearlsAndEquipsItPersistently() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putInteger("progression.pearls", 50);
        ProgressionStore store = new ProgressionStore(preferences);

        assertTrue(store.purchaseSuit(DiverSuit.RESCUE_RED));
        assertEquals(50 - GameConfig.RESCUE_RED_SUIT_COST, store.pearls());
        assertTrue(store.isSuitUnlocked(DiverSuit.RESCUE_RED));
        assertEquals(DiverSuit.RESCUE_RED, new ProgressionStore(preferences).selectedSuit());
        assertFalse(store.purchaseSuit(DiverSuit.RESCUE_RED));
    }

    @Test
    void lockedOrUnaffordableSuitCannotBeEquipped() {
        ProgressionStore store = new ProgressionStore(new MemoryPreferences());

        assertFalse(store.selectSuit(DiverSuit.ABYSS_BLACK));
        assertFalse(store.purchaseSuit(DiverSuit.ABYSS_BLACK));
        assertEquals(DiverSuit.TIDELINE_BLUE, store.selectedSuit());
    }

    @Test
    void everySuitHasOneDistinctSpecialization() {
        assertEquals(GameConfig.TIDELINE_BLUE_RELOAD_MULTIPLIER,
            DiverSuit.TIDELINE_BLUE.reloadMultiplier(), 0.0001f);
        assertEquals(GameConfig.SALVAGE_GREEN_MAGNET_BONUS,
            DiverSuit.SALVAGE_GREEN.magnetBonusRange(), 0.0001f);
        assertEquals(GameConfig.RESCUE_RED_OXYGEN_BONUS,
            DiverSuit.RESCUE_RED.oxygenBonus(), 0.0001f);
        assertEquals(GameConfig.ABYSS_BLACK_AGILITY_MULTIPLIER,
            DiverSuit.ABYSS_BLACK.agilityMultiplier(), 0.0001f);
    }

    private static final class MemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Preferences putBoolean(String key, boolean value) {
            values.put(key, value);
            return this;
        }

        @Override
        public Preferences putInteger(String key, int value) {
            values.put(key, value);
            return this;
        }

        @Override
        public Preferences putLong(String key, long value) {
            values.put(key, value);
            return this;
        }

        @Override
        public Preferences putFloat(String key, float value) {
            values.put(key, value);
            return this;
        }

        @Override
        public Preferences putString(String key, String value) {
            values.put(key, value);
            return this;
        }

        @Override
        public Preferences put(Map<String, ?> entries) {
            values.putAll(entries);
            return this;
        }

        @Override
        public boolean getBoolean(String key) {
            return getBoolean(key, false);
        }

        @Override
        public int getInteger(String key) {
            return getInteger(key, 0);
        }

        @Override
        public long getLong(String key) {
            return getLong(key, 0L);
        }

        @Override
        public float getFloat(String key) {
            return getFloat(key, 0f);
        }

        @Override
        public String getString(String key) {
            return getString(key, "");
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            Object value = values.get(key);
            return value instanceof Boolean ? (Boolean) value : defaultValue;
        }

        @Override
        public int getInteger(String key, int defaultValue) {
            Object value = values.get(key);
            return value instanceof Integer ? (Integer) value : defaultValue;
        }

        @Override
        public long getLong(String key, long defaultValue) {
            Object value = values.get(key);
            return value instanceof Long ? (Long) value : defaultValue;
        }

        @Override
        public float getFloat(String key, float defaultValue) {
            Object value = values.get(key);
            return value instanceof Float ? (Float) value : defaultValue;
        }

        @Override
        public String getString(String key, String defaultValue) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : defaultValue;
        }

        @Override
        public Map<String, ?> get() {
            return Map.copyOf(values);
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public void clear() {
            values.clear();
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }

        @Override
        public void flush() {
        }
    }
}
