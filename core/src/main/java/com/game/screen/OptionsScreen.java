package com.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.game.DeepDiveDrift;
import com.game.GameConfig;
import com.game.diver.Background;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.settings.DisplaySettings;
import com.game.settings.DisplaySettingsStore.WindowMode;

public class OptionsScreen extends BaseScreen {
    private static final String[] OPTIONS = {
        "MUSIC",
        "SOUND EFFECTS",
        "WINDOW MODE",
        "RESOLUTION",
        "VSYNC",
        "FPS LIMIT",
        "ANTIALIASING",
        "SCREEN SHAKE",
        "FLASH EFFECTS",
        "BACK"
    };

    private final Background background;
    private final BitmapFont smallFont;
    private final BitmapFont mediumFont;
    private final BitmapFont largeFont;
    private final GlyphLayout layout;

    private int selectedIndex;

    public OptionsScreen(DeepDiveDrift game) {
        super(game);
        background = new Background(12f);
        smallFont = FontManager.getSmallFont();
        mediumFont = FontManager.getMediumFont();
        largeFont = FontManager.getLargeFont();
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        prepareFrame(0f, 0.04f, 0.1f);
        if (handleInput()) {
            return;
        }

        background.update(Math.min(delta, 0.1f));
        batch.begin();
        background.render(batch);
        batch.end();

        beginFilledShapes();
        shapeRenderer.setColor(0.01f, 0.04f, 0.1f, 0.9f);
        shapeRenderer.rect(250f, 55f, 780f, 610f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.9f);
        shapeRenderer.rect(250f, 660f, 780f, 5f);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "OPTIONS", 625f, Color.WHITE);

        float startY = 535f;
        for (int i = 0; i < OPTIONS.length; i++) {
            Color color = i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY;
            String prefix = i == selectedIndex ? ">  " : "   ";
            drawCentered(mediumFont, prefix + optionLabel(i), startY - i * 46f, color);
        }

        String hint;
        if (selectedIndex == 3 && DisplaySettings.windowMode() == WindowMode.BORDERLESS) {
            hint = "BORDERLESS MODE USES THE DESKTOP RESOLUTION  |  ESC TO GO BACK";
        } else if (DisplaySettings.restartRequired()) {
            hint = "LEFT / RIGHT TO CHANGE  |  ESC BACK  |  ANTIALIASING APPLIES AFTER RESTART";
        } else {
            hint = "LEFT / RIGHT TO CHANGE  |  ENTER TO SELECT  |  ESC TO GO BACK";
        }
        drawCentered(smallFont, hint, 34f, Color.LIGHT_GRAY);
        batch.end();
    }

    private boolean handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = Math.floorMod(selectedIndex - 1, OPTIONS.length);
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % OPTIONS.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            changeSelectedOption(-1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            changeSelectedOption(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedIndex == OPTIONS.length - 1) {
                AudioManager.playConfirm();
                game.closeOverlay();
                return true;
            }
            changeSelectedOption(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            AudioManager.playSelect();
            game.closeOverlay();
            return true;
        }
        return false;
    }

    private void changeSelectedOption(int direction) {
        switch (selectedIndex) {
            case 0 -> AudioManager.toggleMusic();
            case 1 -> AudioManager.toggleSfx();
            case 2 -> DisplaySettings.cycleWindowMode(direction);
            case 3 -> {
                if (DisplaySettings.windowMode() != WindowMode.BORDERLESS) {
                    DisplaySettings.cycleResolution(direction);
                }
            }
            case 4 -> DisplaySettings.toggleVsync();
            case 5 -> DisplaySettings.cycleFpsLimit(direction);
            case 6 -> DisplaySettings.toggleMsaa();
            case 7 -> DisplaySettings.toggleScreenShake();
            case 8 -> DisplaySettings.toggleFlashEffects();
            case 9 -> {
                return;
            }
            default -> throw new IllegalStateException("Unknown option: " + selectedIndex);
        }
        AudioManager.playConfirm();
    }

    private String optionLabel(int index) {
        return switch (index) {
            case 0 -> OPTIONS[index] + ":  " + onOff(AudioManager.isMusicEnabled());
            case 1 -> OPTIONS[index] + ":  " + onOff(AudioManager.isSfxEnabled());
            case 2 -> OPTIONS[index] + ":  " + DisplaySettings.windowMode().name().replace('_', ' ');
            case 3 -> OPTIONS[index] + ":  "
                + (DisplaySettings.windowMode() == WindowMode.BORDERLESS
                ? "DESKTOP" : DisplaySettings.resolution());
            case 4 -> OPTIONS[index] + ":  " + onOff(DisplaySettings.vsyncEnabled());
            case 5 -> OPTIONS[index] + ":  "
                + (DisplaySettings.fpsLimit() == 0 ? "UNLIMITED" : DisplaySettings.fpsLimit());
            case 6 -> OPTIONS[index] + ":  "
                + (DisplaySettings.desiredMsaaSamples() >= 4 ? "4X MSAA" : "OFF");
            case 7 -> OPTIONS[index] + ":  " + onOff(DisplaySettings.screenShakeEnabled());
            case 8 -> OPTIONS[index] + ":  " + onOff(DisplaySettings.flashEffectsEnabled());
            default -> OPTIONS[index];
        };
    }

    private String onOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }
}
