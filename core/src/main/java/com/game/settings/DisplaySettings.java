package com.game.settings;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.game.settings.DisplaySettingsStore.Resolution;
import com.game.settings.DisplaySettingsStore.WindowMode;

import java.util.List;

public final class DisplaySettings {
    private static WindowMode windowMode;
    private static int resolutionIndex;
    private static int windowWidth;
    private static int windowHeight;
    private static boolean vsyncEnabled;
    private static int fpsLimit;
    private static int desiredMsaaSamples;
    private static int activeMsaaSamples;
    private static boolean screenShakeEnabled;
    private static boolean flashEffectsEnabled;
    private static boolean displayConfigurationSupported;
    private static boolean initialized;

    private DisplaySettings() {
    }

    public static void initialize() {
        if (initialized) {
            applyAll();
            return;
        }
        displayConfigurationSupported = Gdx.app.getType() == Application.ApplicationType.Desktop;
        windowMode = DisplaySettingsStore.windowMode();
        windowWidth = DisplaySettingsStore.windowWidth();
        windowHeight = DisplaySettingsStore.windowHeight();
        resolutionIndex = findClosestResolution(windowWidth, windowHeight);
        vsyncEnabled = DisplaySettingsStore.vsyncEnabled();
        fpsLimit = DisplaySettingsStore.fpsLimit();
        desiredMsaaSamples = DisplaySettingsStore.msaaSamples();
        activeMsaaSamples = Gdx.graphics.getBufferFormat().samples;
        screenShakeEnabled = DisplaySettingsStore.screenShakeEnabled();
        flashEffectsEnabled = DisplaySettingsStore.flashEffectsEnabled();
        initialized = true;
        applyAll();
    }

    public static void applyAll() {
        if (displayConfigurationSupported) {
            applyWindowMode();
            Gdx.graphics.setVSync(vsyncEnabled);
            Gdx.graphics.setForegroundFPS(fpsLimit);
        } else {
            Gdx.graphics.setForegroundFPS(60);
        }
    }

    public static void cycleWindowMode(int direction) {
        if (!displayConfigurationSupported) {
            return;
        }
        if (windowMode == WindowMode.WINDOWED && !Gdx.graphics.isFullscreen()) {
            captureCurrentWindowSize();
        }
        windowMode = windowMode.next(direction);
        DisplaySettingsStore.setWindowMode(windowMode);
        applyWindowMode();
    }

    public static void cycleResolution(int direction) {
        if (!displayConfigurationSupported) {
            return;
        }
        resolutionIndex = Math.floorMod(resolutionIndex + direction,
            DisplaySettingsStore.RESOLUTIONS.size());
        Resolution preset = DisplaySettingsStore.RESOLUTIONS.get(resolutionIndex);
        windowWidth = preset.width();
        windowHeight = preset.height();
        DisplaySettingsStore.setWindowSize(windowWidth, windowHeight);
        if (windowMode == WindowMode.WINDOWED) {
            Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
        } else if (windowMode == WindowMode.FULLSCREEN) {
            applyFullscreenResolution();
        }
    }

    public static void toggleVsync() {
        if (!displayConfigurationSupported) {
            return;
        }
        vsyncEnabled = !vsyncEnabled;
        DisplaySettingsStore.setVsyncEnabled(vsyncEnabled);
        Gdx.graphics.setVSync(vsyncEnabled);
    }

    public static void cycleFpsLimit(int direction) {
        if (!displayConfigurationSupported) {
            return;
        }
        int currentIndex = 0;
        for (int i = 0; i < DisplaySettingsStore.FPS_LIMITS.length; i++) {
            if (DisplaySettingsStore.FPS_LIMITS[i] == fpsLimit) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = Math.floorMod(currentIndex + direction, DisplaySettingsStore.FPS_LIMITS.length);
        fpsLimit = DisplaySettingsStore.FPS_LIMITS[nextIndex];
        DisplaySettingsStore.setFpsLimit(fpsLimit);
        Gdx.graphics.setForegroundFPS(fpsLimit);
    }

    public static void toggleMsaa() {
        if (!displayConfigurationSupported) {
            return;
        }
        desiredMsaaSamples = desiredMsaaSamples >= 4 ? 0 : 4;
        DisplaySettingsStore.setMsaaSamples(desiredMsaaSamples);
    }

    public static void toggleScreenShake() {
        screenShakeEnabled = !screenShakeEnabled;
        DisplaySettingsStore.setScreenShakeEnabled(screenShakeEnabled);
    }

    public static void toggleFlashEffects() {
        flashEffectsEnabled = !flashEffectsEnabled;
        DisplaySettingsStore.setFlashEffectsEnabled(flashEffectsEnabled);
    }

    public static void captureWindowSize() {
        if (!initialized || !displayConfigurationSupported
            || windowMode != WindowMode.WINDOWED || Gdx.graphics.isFullscreen()) {
            return;
        }
        captureCurrentWindowSize();
    }

    private static void captureCurrentWindowSize() {
        windowWidth = Gdx.graphics.getWidth();
        windowHeight = Gdx.graphics.getHeight();
        resolutionIndex = findClosestResolution(windowWidth, windowHeight);
        DisplaySettingsStore.setWindowSize(windowWidth, windowHeight);
    }

    public static WindowMode windowMode() {
        return windowMode;
    }

    public static Resolution resolution() {
        return new Resolution(windowWidth, windowHeight);
    }

    public static boolean vsyncEnabled() {
        return vsyncEnabled;
    }

    public static int fpsLimit() {
        return fpsLimit;
    }

    public static int desiredMsaaSamples() {
        return desiredMsaaSamples;
    }

    public static boolean restartRequired() {
        return displayConfigurationSupported && desiredMsaaSamples != activeMsaaSamples;
    }

    public static boolean supportsDisplayConfiguration() {
        return displayConfigurationSupported;
    }

    public static boolean isMobile() {
        if (Gdx.app == null) {
            return false;
        }
        Application.ApplicationType type = Gdx.app.getType();
        return type == Application.ApplicationType.Android || type == Application.ApplicationType.iOS;
    }

    public static boolean screenShakeEnabled() {
        return screenShakeEnabled;
    }

    public static boolean flashEffectsEnabled() {
        return flashEffectsEnabled;
    }

    private static void applyWindowMode() {
        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        switch (windowMode) {
            case WINDOWED -> {
                Gdx.graphics.setUndecorated(false);
                Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
            }
            case BORDERLESS -> {
                Gdx.graphics.setUndecorated(true);
                Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
            }
            case FULLSCREEN -> {
                Gdx.graphics.setUndecorated(false);
                applyFullscreenResolution();
            }
        }
    }

    private static void applyFullscreenResolution() {
        Graphics.DisplayMode selected = null;
        for (Graphics.DisplayMode candidate : Gdx.graphics.getDisplayModes()) {
            if (candidate.width == windowWidth && candidate.height == windowHeight
                && (selected == null || candidate.refreshRate > selected.refreshRate)) {
                selected = candidate;
            }
        }
        if (selected == null) {
            selected = Gdx.graphics.getDisplayMode();
            windowWidth = selected.width;
            windowHeight = selected.height;
            resolutionIndex = findClosestResolution(windowWidth, windowHeight);
            DisplaySettingsStore.setWindowSize(windowWidth, windowHeight);
        }
        Gdx.graphics.setFullscreenMode(selected);
    }

    private static int findClosestResolution(int width, int height) {
        List<Resolution> resolutions = DisplaySettingsStore.RESOLUTIONS;
        int bestIndex = 0;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 0; i < resolutions.size(); i++) {
            Resolution resolution = resolutions.get(i);
            long dx = resolution.width() - width;
            long dy = resolution.height() - height;
            long distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
