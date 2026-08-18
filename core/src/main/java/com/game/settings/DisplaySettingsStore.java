package com.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DisplaySettingsStore {
    public static final int MIN_WINDOW_WIDTH = 640;
    public static final int MIN_WINDOW_HEIGHT = 360;
    public static final int MAX_WINDOW_WIDTH = 7680;
    public static final int MAX_WINDOW_HEIGHT = 4320;
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int[] FPS_LIMITS = {60, 120, 144, 240, 0};
    public static final List<Resolution> RESOLUTIONS = Collections.unmodifiableList(Arrays.asList(
        new Resolution(960, 540),
        new Resolution(1280, 720),
        new Resolution(1600, 900),
        new Resolution(1920, 1080),
        new Resolution(2560, 1440)
    ));

    private static PreferenceBackend backend;

    private DisplaySettingsStore() {
    }

    /**
     * Installs a platform-specific backend. The desktop launcher uses this before libGDX starts,
     * because its window settings are needed while constructing the application.
     */
    public static void installBackend(PreferenceBackend preferenceBackend) {
        backend = Objects.requireNonNull(preferenceBackend, "preferenceBackend");
    }

    public static WindowMode windowMode() {
        String stored = backend().getString("windowMode", WindowMode.WINDOWED.name());
        try {
            return WindowMode.valueOf(stored);
        } catch (IllegalArgumentException exception) {
            return WindowMode.WINDOWED;
        }
    }

    public static void setWindowMode(WindowMode mode) {
        backend().putString("windowMode", mode.name());
        flush();
    }

    public static int windowWidth() {
        return clamp(backend().getInt("windowWidth", DEFAULT_WIDTH),
            MIN_WINDOW_WIDTH, MAX_WINDOW_WIDTH);
    }

    public static int windowHeight() {
        return clamp(backend().getInt("windowHeight", DEFAULT_HEIGHT),
            MIN_WINDOW_HEIGHT, MAX_WINDOW_HEIGHT);
    }

    public static void setWindowSize(int width, int height) {
        backend().putInt("windowWidth", clamp(width, MIN_WINDOW_WIDTH, MAX_WINDOW_WIDTH));
        backend().putInt("windowHeight", clamp(height, MIN_WINDOW_HEIGHT, MAX_WINDOW_HEIGHT));
        flush();
    }

    public static boolean vsyncEnabled() {
        return backend().getBoolean("vsync", true);
    }

    public static void setVsyncEnabled(boolean enabled) {
        backend().putBoolean("vsync", enabled);
        flush();
    }

    public static int fpsLimit() {
        int stored = backend().getInt("fpsLimit", 144);
        for (int value : FPS_LIMITS) {
            if (value == stored) {
                return stored;
            }
        }
        return 144;
    }

    public static void setFpsLimit(int fpsLimit) {
        backend().putInt("fpsLimit", fpsLimit);
        flush();
    }

    public static int msaaSamples() {
        return backend().getInt("msaaSamples", 4) >= 4 ? 4 : 0;
    }

    public static void setMsaaSamples(int samples) {
        backend().putInt("msaaSamples", samples >= 4 ? 4 : 0);
        flush();
    }

    public static boolean screenShakeEnabled() {
        return backend().getBoolean("screenShake", true);
    }

    public static void setScreenShakeEnabled(boolean enabled) {
        backend().putBoolean("screenShake", enabled);
        flush();
    }

    public static boolean flashEffectsEnabled() {
        return backend().getBoolean("flashEffects", true);
    }

    public static void setFlashEffectsEnabled(boolean enabled) {
        backend().putBoolean("flashEffects", enabled);
        flush();
    }

    private static PreferenceBackend backend() {
        if (backend == null) {
            if (Gdx.app == null) {
                throw new IllegalStateException("Display settings were used before a platform backend was available");
            }
            backend = new GdxPreferenceBackend(Gdx.app.getPreferences("DeepDiveDriftDisplay"));
        }
        return backend;
    }

    private static void flush() {
        backend().flush();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public interface PreferenceBackend {
        String getString(String key, String defaultValue);

        int getInt(String key, int defaultValue);

        boolean getBoolean(String key, boolean defaultValue);

        void putString(String key, String value);

        void putInt(String key, int value);

        void putBoolean(String key, boolean value);

        void flush();
    }

    private static final class GdxPreferenceBackend implements PreferenceBackend {
        private final Preferences preferences;

        private GdxPreferenceBackend(Preferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public String getString(String key, String defaultValue) {
            return preferences.getString(key, defaultValue);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return preferences.getInteger(key, defaultValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return preferences.getBoolean(key, defaultValue);
        }

        @Override
        public void putString(String key, String value) {
            preferences.putString(key, value);
        }

        @Override
        public void putInt(String key, int value) {
            preferences.putInteger(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            preferences.putBoolean(key, value);
        }

        @Override
        public void flush() {
            preferences.flush();
        }
    }

    public enum WindowMode {
        WINDOWED,
        BORDERLESS,
        FULLSCREEN;

        public WindowMode next(int direction) {
            WindowMode[] values = values();
            return values[Math.floorMod(ordinal() + direction, values.length)];
        }
    }

    public record Resolution(int width, int height) {
        @Override
        public String toString() {
            return width + "x" + height;
        }
    }
}
