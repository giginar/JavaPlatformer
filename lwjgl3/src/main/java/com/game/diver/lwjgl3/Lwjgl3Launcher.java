package com.game.diver.lwjgl3;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.game.DeepDiveDrift;
import com.game.settings.DisplaySettingsStore;
import com.game.settings.DisplaySettingsStore.WindowMode;

import java.util.Locale;

/** Launches the desktop (LWJGL3) application. */
public final class Lwjgl3Launcher {
    private static final String DEBUG_PROPERTY = "deepdive.debug";

    private Lwjgl3Launcher() {
    }

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }
        DisplaySettingsStore.installBackend(new DesktopPreferencesBackend());
        applyLaunchArguments(args);
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new DeepDiveDrift(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("DeepDive Drift");
        configuration.setHdpiMode(HdpiMode.Logical);
        configuration.useVsync(DisplaySettingsStore.vsyncEnabled());
        configuration.setForegroundFPS(DisplaySettingsStore.fpsLimit());
        configuration.setIdleFPS(30);
        configuration.setBackBufferConfig(8, 8, 8, 8, 24, 8,
            DisplaySettingsStore.msaaSamples());

        configuration.setWindowSizeLimits(
            DisplaySettingsStore.MIN_WINDOW_WIDTH,
            DisplaySettingsStore.MIN_WINDOW_HEIGHT,
            -1,
            -1
        );
        configuration.setResizable(true);
        configuration.setAutoIconify(false);
        applyInitialWindowMode(configuration);

        configuration.setWindowIcon(
            "deepdive128.png",
            "deepdive64.png",
            "deepdive32.png",
            "deepdive16.png"
        );
        if (Boolean.getBoolean(DEBUG_PROPERTY)) {
            configuration.enableGLDebugOutput(true, System.err);
        }
        return configuration;
    }

    private static void applyInitialWindowMode(Lwjgl3ApplicationConfiguration configuration) {
        WindowMode mode = DisplaySettingsStore.windowMode();
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        switch (mode) {
            case WINDOWED -> {
                configuration.setDecorated(true);
                configuration.setWindowedMode(
                    DisplaySettingsStore.windowWidth(),
                    DisplaySettingsStore.windowHeight()
                );
            }
            case BORDERLESS -> {
                configuration.setDecorated(false);
                configuration.setWindowedMode(displayMode.width, displayMode.height);
            }
            case FULLSCREEN -> configuration.setFullscreenMode(findFullscreenMode(displayMode));
        }
    }

    private static Graphics.DisplayMode findFullscreenMode(Graphics.DisplayMode fallback) {
        int requestedWidth = DisplaySettingsStore.windowWidth();
        int requestedHeight = DisplaySettingsStore.windowHeight();
        if (fallback.width == requestedWidth && fallback.height == requestedHeight) {
            return fallback;
        }

        Graphics.DisplayMode selected = null;
        int closestRefreshRate = Integer.MAX_VALUE;
        for (Graphics.DisplayMode candidate : Lwjgl3ApplicationConfiguration.getDisplayModes()) {
            if (candidate.width == requestedWidth && candidate.height == requestedHeight
                && (selected == null
                || Math.abs(candidate.refreshRate - fallback.refreshRate) < closestRefreshRate
                || Math.abs(candidate.refreshRate - fallback.refreshRate) == closestRefreshRate
                && candidate.bitsPerPixel > selected.bitsPerPixel)) {
                selected = candidate;
                closestRefreshRate = Math.abs(candidate.refreshRate - fallback.refreshRate);
            }
        }
        return selected == null ? fallback : selected;
    }

    private static void applyLaunchArguments(String[] args) {
        for (String argument : args) {
            switch (argument.toLowerCase(Locale.ROOT)) {
                case "--debug" -> System.setProperty(DEBUG_PROPERTY, "true");
                case "--autostart" -> System.setProperty("deepdive.autostart", "true");
                case "--open-options" -> System.setProperty("deepdive.openOptions", "true");
                case "--open-store" -> System.setProperty("deepdive.openStore", "true");
                case "--open-setup" -> System.setProperty("deepdive.openSetup", "true");
                case "--open-achievements" ->
                    System.setProperty("deepdive.openAchievements", "true");
                case "--open-suits" -> {
                    System.setProperty("deepdive.openStore", "true");
                    System.setProperty("deepdive.openSuits", "true");
                }
                case "--capture-exit" -> System.setProperty("deepdive.capture.exit", "true");
                case "--capture-autoplay" -> System.setProperty("deepdive.capture.autoplay", "true");
                case "--capture-boss" -> System.setProperty("deepdive.capture.boss", "true");
                case "--hide-tutorial" -> System.setProperty("deepdive.hideTutorial", "true");
                case "--windowed" -> DisplaySettingsStore.setWindowMode(WindowMode.WINDOWED);
                case "--borderless" -> DisplaySettingsStore.setWindowMode(WindowMode.BORDERLESS);
                case "--fullscreen" -> DisplaySettingsStore.setWindowMode(WindowMode.FULLSCREEN);
                case "--vsync" -> DisplaySettingsStore.setVsyncEnabled(true);
                case "--no-vsync" -> DisplaySettingsStore.setVsyncEnabled(false);
                case "--msaa" -> DisplaySettingsStore.setMsaaSamples(4);
                case "--no-msaa" -> DisplaySettingsStore.setMsaaSamples(0);
                default -> applyValueArgument(argument);
            }
        }
    }

    private static void applyValueArgument(String argument) {
        if (argument.regionMatches(true, 0, "--stage-duration=", 0, 17)) {
            try {
                float seconds = Float.parseFloat(argument.substring(17));
                if (seconds >= 5f && seconds <= 600f) {
                    System.setProperty("deepdive.stageDurationSeconds", Float.toString(seconds));
                    return;
                }
            } catch (NumberFormatException ignored) {
                // The warning below also covers non-numeric values.
            }
            System.err.println("Ignoring invalid stage duration: " + argument);
            return;
        }

        if (argument.regionMatches(true, 0, "--capture=", 0, 10)) {
            String outputPath = argument.substring(10).trim();
            if (!outputPath.isEmpty()) {
                System.setProperty("deepdive.capture.path", outputPath);
            }
            return;
        }

        if (argument.regionMatches(true, 0, "--capture-delay=", 0, 16)) {
            try {
                float delay = Float.parseFloat(argument.substring(16));
                if (delay >= 0f && delay <= 60f) {
                    System.setProperty("deepdive.capture.delay", Float.toString(delay));
                    return;
                }
            } catch (NumberFormatException ignored) {
                // The warning below also covers non-numeric values.
            }
            System.err.println("Ignoring invalid capture delay: " + argument);
            return;
        }

        if (argument.regionMatches(true, 0, "--resolution=", 0, 13)) {
            String[] dimensions = argument.substring(13).toLowerCase(Locale.ROOT).split("x", 2);
            if (dimensions.length == 2) {
                try {
                    DisplaySettingsStore.setWindowSize(
                        Integer.parseInt(dimensions[0]),
                        Integer.parseInt(dimensions[1])
                    );
                } catch (NumberFormatException ignored) {
                    System.err.println("Ignoring invalid resolution argument: " + argument);
                }
            }
            return;
        }

        if (argument.regionMatches(true, 0, "--fps=", 0, 6)) {
            try {
                int requestedFps = Integer.parseInt(argument.substring(6));
                for (int supportedFps : DisplaySettingsStore.FPS_LIMITS) {
                    if (requestedFps == supportedFps) {
                        DisplaySettingsStore.setFpsLimit(requestedFps);
                        return;
                    }
                }
            } catch (NumberFormatException ignored) {
                // The warning below also covers non-numeric values.
            }
            System.err.println("Ignoring unsupported FPS limit: " + argument);
        }
    }
}
