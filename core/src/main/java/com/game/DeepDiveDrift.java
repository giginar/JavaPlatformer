package com.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.manager.GameAssets;
import com.game.screen.GameScreen;
import com.game.screen.MainMenuScreen;
import com.game.screen.OptionsScreen;
import com.game.settings.DisplaySettings;

import java.util.ArrayDeque;
import java.util.Deque;

public class DeepDiveDrift extends Game {
    private final Deque<Screen> suspendedScreens = new ArrayDeque<>();

    @Override
    public void create() {
        DisplaySettings.initialize();
        GameAssets.initialize();
        FontManager.initialize();
        AudioManager.initialize();
        showMainMenu();
    }

    public void showMainMenu() {
        replaceScreen(new MainMenuScreen(this));
    }

    public void startNewGame() {
        replaceScreen(new GameScreen(this));
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
