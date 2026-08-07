package com.game.settings;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class DisplaySettingsStore {
    public static final int MIN_WINDOW_WIDTH = 640;
    public static final int MIN_WINDOW_HEIGHT = 360;
    public static final int MAX_WINDOW_WIDTH = 7680;
    public static final int MAX_WINDOW_HEIGHT = 4320;
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int[] FPS_LIMITS = {60, 120, 144, 240, 0};
    public static final List<Resolution> RESOLUTIONS = List.of(
        new Resolution(960, 540),
        new Resolution(1280, 720),
        new Resolution(1600, 900),
        new Resolution(1920, 1080),
        new Resolution(2560, 1440)
    );

    private static final Preferences PREFS = Preferences.userRoot().node("com/game/deepdive-drift/display");

    private DisplaySettingsStore() {
    }

    public static WindowMode windowMode() {
        String stored = PREFS.get("windowMode", WindowMode.WINDOWED.name());
        try {
            return WindowMode.valueOf(stored);
        } catch (IllegalArgumentException exception) {
            return WindowMode.WINDOWED;
        }
    }

    public static void setWindowMode(WindowMode mode) {
        PREFS.put("windowMode", mode.name());
        flush();
    }

    public static int windowWidth() {
        return clamp(PREFS.getInt("windowWidth", DEFAULT_WIDTH),
            MIN_WINDOW_WIDTH, MAX_WINDOW_WIDTH);
    }

    public static int windowHeight() {
        return clamp(PREFS.getInt("windowHeight", DEFAULT_HEIGHT),
            MIN_WINDOW_HEIGHT, MAX_WINDOW_HEIGHT);
    }

    public static void setWindowSize(int width, int height) {
        PREFS.putInt("windowWidth", clamp(width, MIN_WINDOW_WIDTH, MAX_WINDOW_WIDTH));
        PREFS.putInt("windowHeight", clamp(height, MIN_WINDOW_HEIGHT, MAX_WINDOW_HEIGHT));
        flush();
    }

    public static boolean vsyncEnabled() {
        return PREFS.getBoolean("vsync", true);
    }

    public static void setVsyncEnabled(boolean enabled) {
        PREFS.putBoolean("vsync", enabled);
        flush();
    }

    public static int fpsLimit() {
        int stored = PREFS.getInt("fpsLimit", 144);
        for (int value : FPS_LIMITS) {
            if (value == stored) {
                return stored;
            }
        }
        return 144;
    }

    public static void setFpsLimit(int fpsLimit) {
        PREFS.putInt("fpsLimit", fpsLimit);
        flush();
    }

    public static int msaaSamples() {
        return PREFS.getInt("msaaSamples", 4) >= 4 ? 4 : 0;
    }

    public static void setMsaaSamples(int samples) {
        PREFS.putInt("msaaSamples", samples >= 4 ? 4 : 0);
        flush();
    }

    public static boolean screenShakeEnabled() {
        return PREFS.getBoolean("screenShake", true);
    }

    public static void setScreenShakeEnabled(boolean enabled) {
        PREFS.putBoolean("screenShake", enabled);
        flush();
    }

    public static boolean flashEffectsEnabled() {
        return PREFS.getBoolean("flashEffects", true);
    }

    public static void setFlashEffectsEnabled(boolean enabled) {
        PREFS.putBoolean("flashEffects", enabled);
        flush();
    }

    private static void flush() {
        try {
            PREFS.flush();
        } catch (BackingStoreException exception) {
            System.err.println("Could not persist display settings: " + exception.getMessage());
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
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
