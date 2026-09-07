package com.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.game.input.GameInput;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.manager.GameAssets;
import com.game.screen.GameScreen;
import com.game.screen.AchievementScreen;
import com.game.screen.DiveSetupScreen;
import com.game.screen.MainMenuScreen;
import com.game.screen.OptionsScreen;
import com.game.screen.StoreScreen;
import com.game.screen.SoundChoiceScreen;
import com.game.settings.DisplaySettings;
import com.game.model.RunSettings;

import java.util.ArrayDeque;
import java.util.Deque;

public class DeepDiveDrift extends Game {
    private final Deque<Screen> suspendedScreens = new ArrayDeque<>();
    private GameInput input;
    private float captureElapsed;
    private boolean captureCompleted;

    @Override
    public void create() {
        DisplaySettings.initialize();
        input = new GameInput();
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        GameAssets.initialize();
        FontManager.initialize();
        AudioManager.initialize();
        if (AudioManager.needsStartupSoundChoice()) {
            setScreen(new SoundChoiceScreen(this));
            return;
        }
        if (Boolean.getBoolean("deepdive.openAchievements")) {
            showAchievements();
        } else if (Boolean.getBoolean("deepdive.openSetup")) {
            showDiveSetup();
        } else if (Boolean.getBoolean("deepdive.openStore")) {
            showStore();
        } else if (Boolean.getBoolean("deepdive.autostart")) {
            startNewGame();
        } else {
            showMainMenu();
            if (Boolean.getBoolean("deepdive.openOptions")) {
                openOptions();
            }
        }
    }

    public GameInput input() {
        return input;
    }

    @Override
    public void render() {
        super.render();
        captureFrameIfRequested();
    }

    private void captureFrameIfRequested() {
        String capturePath = System.getProperty("deepdive.capture.path");
        if (captureCompleted || capturePath == null || capturePath.trim().isEmpty()) {
            return;
        }

        captureElapsed += Math.min(Gdx.graphics.getDeltaTime(), 0.1f);
        float delay = Float.parseFloat(System.getProperty("deepdive.capture.delay", "1.0"));
        if (captureElapsed < delay) {
            return;
        }

        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);
        Pixmap screenshot = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, screenshot.getPixels(), pixels.length);
        try {
            var output = Gdx.files.absolute(capturePath);
            output.parent().mkdirs();
            PixmapIO.writePNG(output, screenshot);
            captureCompleted = true;
            System.out.println("Captured storefront screenshot: " + output.file().getAbsolutePath());
        } finally {
            screenshot.dispose();
        }

        if (Boolean.getBoolean("deepdive.capture.exit")) {
            Gdx.app.exit();
        }
    }

    public void showMainMenu() {
        replaceScreen(new MainMenuScreen(this));
    }

    public void startNewGame() {
        startNewGame(RunSettings.standard());
    }

    public void startNewGame(RunSettings settings) {
        replaceScreen(new GameScreen(this, settings));
    }

    public void showDiveSetup() {
        replaceScreen(new DiveSetupScreen(this));
    }

    public void showAchievements() {
        replaceScreen(new AchievementScreen(this));
    }

    public void showStore() {
        replaceScreen(new StoreScreen(this));
    }

    public void openOptions() {
        Screen current = getScreen();
        if (current != null) {
            suspendedScreens.push(current);
        }
        setScreen(new OptionsScreen(this));
    }

    public void closeOverlay() {
        Screen overlay = getScreen();
        Screen previous = suspendedScreens.poll();
        if (previous == null) {
            showMainMenu();
            return;
        }

        setScreen(previous);
        if (overlay != null) {
            overlay.dispose();
        }
    }

    private void replaceScreen(Screen nextScreen) {
        Screen current = getScreen();
        setScreen(nextScreen);

        if (current != null && current != nextScreen) {
            current.dispose();
        }
        while (!suspendedScreens.isEmpty()) {
            Screen suspended = suspendedScreens.pop();
            if (suspended != current && suspended != nextScreen) {
                suspended.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        DisplaySettings.captureWindowSize();
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        while (!suspendedScreens.isEmpty()) {
            Screen suspended = suspendedScreens.pop();
            if (suspended != current) {
                suspended.dispose();
            }
        }

        AudioManager.dispose();
        FontManager.dispose();
        GameAssets.dispose();
    }
}
