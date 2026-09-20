package com.game.settings;

import com.game.settings.DisplaySettingsStore.PreferenceBackend;
import com.game.settings.DisplaySettingsStore.TextScale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplaySettingsStoreTest {
    private MemoryBackend backend;

    @BeforeEach
    void setUp() {
        backend = new MemoryBackend();
        DisplaySettingsStore.installBackend(backend);
    }

    @Test
    void textScaleDefaultsAndPersists() {
        assertEquals(TextScale.DEFAULT, DisplaySettingsStore.textScale());

        DisplaySettingsStore.setTextScale(TextScale.LARGE);

        assertEquals(TextScale.LARGE, DisplaySettingsStore.textScale());
        assertEquals(1, backend.flushes);
    }

    @Test
    void invalidTextScaleFallsBackSafely() {
        backend.putString("textScale", "HUGE");
        assertEquals(TextScale.DEFAULT, DisplaySettingsStore.textScale());
    }

    @Test
    void textScaleCyclesInBothDirections() {
        assertEquals(TextScale.LARGE, TextScale.DEFAULT.next(1));
        assertEquals(TextScale.LARGE, TextScale.DEFAULT.next(-1));
        assertEquals(TextScale.DEFAULT, TextScale.LARGE.next(1));
    }

    private static final class MemoryBackend implements PreferenceBackend {
        private final Map<String, Object> values = new HashMap<>();
        private int flushes;

        @Override public String getString(String key, String fallback) {
            return (String) values.getOrDefault(key, fallback);
        }
        @Override public int getInt(String key, int fallback) {
            return (int) values.getOrDefault(key, fallback);
        }
        @Override public boolean getBoolean(String key, boolean fallback) {
            return (boolean) values.getOrDefault(key, fallback);
        }
        @Override public void putString(String key, String value) { values.put(key, value); }
        @Override public void putInt(String key, int value) { values.put(key, value); }
        @Override public void putBoolean(String key, boolean value) { values.put(key, value); }
        @Override public void flush() { flushes++; }
    }
}
