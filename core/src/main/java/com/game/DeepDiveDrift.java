package com.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.game.ads.AdvertisingService;
import com.game.ads.InterstitialPolicy;
import com.game.ads.NoOpAdvertisingService;
import com.game.input.GameInput;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.manager.GameAssets;
import com.game.screen.GameScreen;
import com.game.screen.AchievementScreen;
import com.game.screen.AboutScreen;
import com.game.screen.DiveSetupScreen;
import com.game.screen.MainMenuScreen;
import com.game.screen.OptionsScreen;
import com.game.screen.ControlsScreen;
import com.game.screen.StoreScreen;
import com.game.screen.SoundChoiceScreen;
import com.game.settings.DisplaySettings;
import com.game.model.RunSettings;
import com.game.model.SaveSchema;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class DeepDiveDrift extends Game {
    private final Deque<Screen> suspendedScreens = new ArrayDeque<>();
    private final AdvertisingService advertising;
    private final InterstitialPolicy interstitialPolicy = new InterstitialPolicy();
    private GameInput input;
    private float captureElapsed;
    private boolean captureCompleted;
    private boolean fullScreenPauseApplied;
    private long resultMenuOpportunity;

    public DeepDiveDrift() {
        this(new NoOpAdvertisingService());
    }

    public DeepDiveDrift(AdvertisingService advertising) {
        this.advertising = advertising;
    }

    @Override
    public void create() {
        DisplaySettings.initialize();
        SaveSchema.migrate(Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME));
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
            if (Boolean.getBoolean("deepdive.openAbout")) {
                openAbout();
            } else if (Boolean.getBoolean("deepdive.openOptions")) {
                openOptions();
            }
        }
    }

    public GameInput input() {
        return input;
    }

    public AdvertisingService advertising() {
        return advertising;
    }

    @Override
    public void render() {
        updateFullScreenLifecycle();
        super.render();
        captureFrameIfRequested();
    }

    private void updateFullScreenLifecycle() {
        boolean fullScreenActive = advertising.isFullScreenContentActive();
        if (fullScreenActive) {
            if (input != null) {
                input.resetAfterLifecyclePause();
            }
            if (!fullScreenPauseApplied) {
                AudioManager.pauseForLifecycle();
                fullScreenPauseApplied = true;
            }
        } else if (fullScreenPauseApplied) {
            if (input != null) {
                input.resetAfterLifecyclePause();
            }
            AudioManager.resumeFromLifecycle();
            fullScreenPauseApplied = false;
        }
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

    public void recordCompletedRun() {
        interstitialPolicy.recordCompletedRun();
    }

    /** Handles the only interstitial opportunity: a completed result screen returning to menu. */
    public void returnToMenuFromResults() {
        long opportunity = ++resultMenuOpportunity;
        boolean eligible = interstitialPolicy.evaluateResultsToMenuOpportunity(
            opportunity,
            System.currentTimeMillis(),
            false,
            advertising.canRequestAds(),
            advertising.isInterstitialAvailable(),
            advertising.isFullScreenContentActive()
        );
        if (!eligible || !advertising.showInterstitial(new AdvertisingService.FullScreenCallback() {
            @Override
            public void onOpened() {
                postToGameThread(() -> {
                    interstitialPolicy.recordInterstitialDisplay(System.currentTimeMillis());
                    pauseForFullScreenContent();
                });
            }

            @Override
            public void onClosed() {
                postToGameThread(() -> {
                    resumeAfterFullScreenContent();
                    showMainMenu();
                });
            }
        })) {
            showMainMenu();
        }
    }

    public boolean showPrivacyOptions() {
        return advertising.showPrivacyOptions(new AdvertisingService.FullScreenCallback() {
            @Override
            public void onOpened() {
                postToGameThread(DeepDiveDrift.this::pauseForFullScreenContent);
            }

            @Override
            public void onClosed() {
                postToGameThread(DeepDiveDrift.this::resumeAfterFullScreenContent);
            }
        });
    }

    /** Technical rewarded path; no screen offers a gameplay reward until product approval. */
    public boolean showRewarded(AdvertisingService.RewardedCallback callback) {
        Objects.requireNonNull(callback, "callback");
        return advertising.showRewarded(new AdvertisingService.RewardedCallback() {
            @Override
            public void onOpened() {
                interstitialPolicy.recordRewardedDisplay();
                postToGameThread(callback::onOpened);
            }

            @Override
            public void onRewardEarned() {
                postToGameThread(callback::onRewardEarned);
            }

            @Override
            public void onClosed() {
                postToGameThread(callback::onClosed);
            }
        });
    }

    private void pauseForFullScreenContent() {
        if (input != null) {
            input.resetAfterLifecyclePause();
        }
        AudioManager.pauseForLifecycle();
    }

    private void resumeAfterFullScreenContent() {
        if (input != null) {
            input.resetAfterLifecyclePause();
        }
        AudioManager.resumeFromLifecycle();
    }

    private void postToGameThread(Runnable action) {
        if (Gdx.app == null) {
            action.run();
        } else {
            Gdx.app.postRunnable(action);
        }
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
        openOverlay(new OptionsScreen(this));
    }

    public void openControls() {
        openOverlay(new ControlsScreen(this));
    }

    public void openAbout() {
        openOverlay(new AboutScreen(this));
    }

    @Override
    public void pause() {
        if (input != null) {
            input.resetAfterLifecyclePause();
        }
        super.pause();
        AudioManager.pauseForLifecycle();
    }

    @Override
    public void resume() {
        super.resume();
        if (advertising.isFullScreenContentActive()) {
            AudioManager.pauseForLifecycle();
            fullScreenPauseApplied = true;
        } else {
            AudioManager.resumeFromLifecycle();
        }
    }

    private void openOverlay(Screen overlay) {
        Screen current = getScreen();
        if (current != null) {
            suspendedScreens.push(current);
        }
        setScreen(overlay);
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

        advertising.dispose();
        AudioManager.dispose();
        FontManager.dispose();
        GameAssets.dispose();
    }
}
