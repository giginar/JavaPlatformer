package com.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;
import com.game.GameConfig;
import com.game.diver.Background;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.settings.DisplaySettings;
import com.game.settings.DisplaySettingsStore.WindowMode;

public class OptionsScreen extends BaseScreen {
    private static final Option[] DESKTOP_OPTIONS = Option.values();
    private static final Option[] MOBILE_OPTIONS = {
        Option.MUSIC,
        Option.SOUND_EFFECTS,
        Option.SCREEN_SHAKE,
        Option.FLASH_EFFECTS,
        Option.CONTROLS,
        Option.BACK
    };

    private final Background background;
    private final BitmapFont smallFont;
    private final BitmapFont mediumFont;
    private final BitmapFont largeFont;
    private final GlyphLayout layout;
    private final Rectangle optionRow = new Rectangle();
    private final Option[] options;

    private int selectedIndex;

    public OptionsScreen(DeepDiveDrift game) {
        super(game);
        background = new Background(12f);
        smallFont = FontManager.getSmallFont();
        mediumFont = FontManager.getMediumFont();
        largeFont = FontManager.getLargeFont();
        layout = new GlyphLayout();
        options = DisplaySettings.supportsDisplayConfiguration() ? DESKTOP_OPTIONS : MOBILE_OPTIONS;
    }

    @Override
    public void render(float delta) {
        prepareFrame(0f, 0.04f, 0.1f);
        updateInput();
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
        for (int i = 0; i < options.length; i++) {
            rowBounds(i);
            drawUiButton(optionRow, i == selectedIndex);
        }
        endShapes();

        batch.begin();
        drawCentered(largeFont, "OPTIONS", 625f, Color.WHITE);

        for (int i = 0; i < options.length; i++) {
            Color color = i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY;
            String prefix = i == selectedIndex ? ">  " : "   ";
            rowBounds(i);
            drawUiButtonLabel(mediumFont, prefix + optionLabel(options[i]), optionRow, color);
        }

        if (options[selectedIndex] == Option.RESOLUTION
            && DisplaySettings.windowMode() == WindowMode.BORDERLESS) {
            drawCentered(smallFont, "BORDERLESS MODE USES THE DESKTOP RESOLUTION",
                34f, Color.LIGHT_GRAY);
        } else if (DisplaySettings.restartRequired()) {
            drawCentered(smallFont, "ANTIALIASING APPLIES AFTER RESTART",
                34f, Color.LIGHT_GRAY);
        }
        batch.end();
    }

    private boolean handleInput() {
        for (int i = 0; i < options.length; i++) {
            rowBounds(i);
            if (game.input().pointerOver(optionRow) && selectedIndex != i) {
                selectedIndex = i;
                AudioManager.playSelect();
            }
            if (game.input().buttonJustReleased(optionRow)) {
                selectedIndex = i;
                return activateSelectedOption();
            }
        }

        if (game.input().menuUpJustPressed()) {
            selectedIndex = Math.floorMod(selectedIndex - 1, options.length);
            AudioManager.playSelect();
        } else if (game.input().menuDownJustPressed()) {
            selectedIndex = (selectedIndex + 1) % options.length;
            AudioManager.playSelect();
        } else if (game.input().menuLeftJustPressed()) {
            changeSelectedOption(-1);
        } else if (game.input().menuRightJustPressed()) {
            changeSelectedOption(1);
        } else if (game.input().confirmJustPressed()) {
            return activateSelectedOption();
        } else if (game.input().backJustPressed()) {
            AudioManager.playSelect();
            game.closeOverlay();
            return true;
        }
        return false;
    }

    private boolean activateSelectedOption() {
        if (options[selectedIndex] == Option.BACK) {
            AudioManager.playConfirm();
            game.closeOverlay();
            return true;
        }
        if (options[selectedIndex] == Option.CONTROLS) {
            AudioManager.playConfirm();
            game.openControls();
            return true;
        }
        changeSelectedOption(1);
        return false;
    }

    private void changeSelectedOption(int direction) {
        switch (options[selectedIndex]) {
            case MUSIC -> AudioManager.toggleMusic();
            case SOUND_EFFECTS -> AudioManager.toggleSfx();
            case WINDOW_MODE -> DisplaySettings.cycleWindowMode(direction);
            case RESOLUTION -> {
                if (DisplaySettings.windowMode() != WindowMode.BORDERLESS) {
                    DisplaySettings.cycleResolution(direction);
                }
            }
            case VSYNC -> DisplaySettings.toggleVsync();
            case FPS_LIMIT -> DisplaySettings.cycleFpsLimit(direction);
            case ANTIALIASING -> DisplaySettings.toggleMsaa();
            case SCREEN_SHAKE -> DisplaySettings.toggleScreenShake();
            case FLASH_EFFECTS -> DisplaySettings.toggleFlashEffects();
            case CONTROLS, BACK -> {
                return;
            }
        }
        AudioManager.playConfirm();
    }

    private String optionLabel(Option option) {
        return switch (option) {
            case MUSIC -> option.label + ":  " + onOff(AudioManager.isMusicEnabled());
            case SOUND_EFFECTS -> option.label + ":  " + onOff(AudioManager.isSfxEnabled());
            case WINDOW_MODE -> option.label + ":  " + DisplaySettings.windowMode().name().replace('_', ' ');
            case RESOLUTION -> option.label + ":  "
                + (DisplaySettings.windowMode() == WindowMode.BORDERLESS
                ? "DESKTOP" : DisplaySettings.resolution());
            case VSYNC -> option.label + ":  " + onOff(DisplaySettings.vsyncEnabled());
            case FPS_LIMIT -> option.label + ":  "
                + (DisplaySettings.fpsLimit() == 0 ? "UNLIMITED" : DisplaySettings.fpsLimit());
            case ANTIALIASING -> option.label + ":  "
                + (DisplaySettings.desiredMsaaSamples() >= 4 ? "4X MSAA" : "OFF");
            case SCREEN_SHAKE -> option.label + ":  " + onOff(DisplaySettings.screenShakeEnabled());
            case FLASH_EFFECTS -> option.label + ":  " + onOff(DisplaySettings.flashEffectsEnabled());
            case CONTROLS, BACK -> option.label;
        };
    }

    private Rectangle rowBounds(int index) {
        float baseline = optionStartY() - index * optionSpacing();
        return optionRow.set(270f, baseline - 30f, 740f, 38f);
    }

    private float optionStartY() {
        return options.length <= MOBILE_OPTIONS.length ? 500f : 535f;
    }

    private float optionSpacing() {
        return options.length <= MOBILE_OPTIONS.length ? 72f : 42f;
    }

    private String onOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }

    private enum Option {
        MUSIC("MUSIC"),
        SOUND_EFFECTS("SOUND EFFECTS"),
        WINDOW_MODE("WINDOW MODE"),
        RESOLUTION("RESOLUTION"),
        VSYNC("VSYNC"),
        FPS_LIMIT("FPS LIMIT"),
        ANTIALIASING("ANTIALIASING"),
        SCREEN_SHAKE("SCREEN SHAKE"),
        FLASH_EFFECTS("FLASH EFFECTS"),
        CONTROLS("CONTROLS"),
        BACK("BACK");

        private final String label;

        Option(String label) {
            this.label = label;
        }
    }
}
