package com.game.diver.lwjgl3;

import com.game.settings.DisplaySettingsStore.PreferenceBackend;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

final class DesktopPreferencesBackend implements PreferenceBackend {
    private final Preferences preferences = Preferences.userRoot()
        .node("com/game/deepdive-drift/display");

    @Override
    public String getString(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public void putString(String key, String value) {
        preferences.put(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        preferences.putInt(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        preferences.putBoolean(key, value);
    }

    @Override
    public void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException exception) {
            System.err.println("Could not persist display settings: " + exception.getMessage());
        }
    }
}
