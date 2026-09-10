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

public class MainMenuScreen extends BaseScreen {
    private static final String[] DESKTOP_MENU_OPTIONS = {
        "PLAY", "DIVE SHOP", "ACHIEVEMENTS", "SOUND", "CONTROLS", "OPTIONS", "EXIT"
    };
    private static final String[] MOBILE_MENU_OPTIONS = {
        "PLAY", "DIVE SHOP", "ACHIEVEMENTS", "SOUND", "CONTROLS", "OPTIONS"
    };
    private static final float TRANSITION_DURATION = 0.45f;
    private static final Color ACCENT_COLOR = new Color(0.55f, 0.9f, 1f, 1f);
    private static final float FIRST_ROW_Y = 500f;
    private static final float ROW_SPACING = 68f;
    private static final float SOUND_ROW_Y = FIRST_ROW_Y - 3f * ROW_SPACING;
    private static final Rectangle VOLUME_DOWN = new Rectangle(415f, SOUND_ROW_Y - 36f, 64f, 56f);
    private static final Rectangle VOLUME_UP = new Rectangle(801f, SOUND_ROW_Y - 36f, 64f, 56f);

    private final Background background;
    private final BitmapFont smallFont;
    private final BitmapFont mediumFont;
    private final BitmapFont largeFont;
    private final GlyphLayout layout;
    private final Rectangle menuRow = new Rectangle();
    private final String[] menuOptions;

    private int selectedIndex;
    private boolean transitioning;
    private float transitionAlpha;

    public MainMenuScreen(DeepDiveDrift game) {
        super(game);
        background = new Background(24f);
        smallFont = FontManager.getSmallFont();
        mediumFont = FontManager.getMediumFont();
        largeFont = FontManager.getLargeFont();
        layout = new GlyphLayout();
        menuOptions = game.input().isMobile() ? MOBILE_MENU_OPTIONS : DESKTOP_MENU_OPTIONS;
    }

    @Override
    public void show() {
        AudioManager.playBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 0.1f);
        prepareFrame(0f, 0.05f, 0.12f);
        updateInput();

        if (!transitioning && handleInput()) {
            return;
        }

        background.update(frameDelta);
        if (transitioning) {
            transitionAlpha = Math.min(1f, transitionAlpha + frameDelta / TRANSITION_DURATION);
        }

        batch.begin();
        background.render(batch);
        batch.end();

        beginFilledShapes();
        for (int i = 0; i < menuOptions.length; i++) {
            rowBounds(i);
            boolean soundAdjustment = i == 3 && (game.input().pointerOver(VOLUME_DOWN)
                || game.input().pointerOver(VOLUME_UP));
            drawUiButton(menuRow, i == selectedIndex && !soundAdjustment);
        }
        drawUiButton(VOLUME_DOWN, false);
        drawUiButton(VOLUME_UP, false);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "DEEP DIVE DRIFT", 644f, Color.WHITE);
        drawCentered(smallFont, "SURVIVE THE ABYSS", 580f, ACCENT_COLOR);

        for (int i = 0; i < menuOptions.length; i++) {
            rowBounds(i);
            Color color = i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY;
            String prefix = i == selectedIndex ? ">  " : "   ";
            if ("SOUND".equals(menuOptions[i])) {
                int volume = Math.round(AudioManager.getMasterVolume() * 100f);
                drawUiButtonLabel(mediumFont, volume == 0 ? "SOUND OFF" : "SOUND " + volume + "%",
                    menuRow, color);
            } else {
                drawUiButtonLabel(mediumFont, prefix + menuOptions[i], menuRow, color);
            }
        }
        drawUiButtonLabel(mediumFont, "-", VOLUME_DOWN, Color.WHITE);
        drawUiButtonLabel(mediumFont, "+", VOLUME_UP, Color.WHITE);

        batch.end();

        if (transitioning) {
            beginFilledShapes();
            shapeRenderer.setColor(0f, 0f, 0f, transitionAlpha);
            shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
            endShapes();

            if (transitionAlpha >= 1f) {
                game.showDiveSetup();
            }
        }
    }

    private boolean handleInput() {
        // Process sound controls before hover/confirmation sounds, so muting is silent.
        if (game.input().buttonJustReleased(VOLUME_DOWN)) {
            selectedIndex = 3;
            changeVolume(-0.25f);
            return false;
        }
        if (game.input().buttonJustReleased(VOLUME_UP)) {
            selectedIndex = 3;
            changeVolume(0.25f);
            return false;
        }
        for (int i = 0; i < menuOptions.length; i++) {
            rowBounds(i);
            if ("SOUND".equals(menuOptions[i]) && game.input().buttonJustReleased(menuRow)) {
                selectedIndex = i;
                AudioManager.toggleMute();
                return false;
            }
            if (game.input().pointerOver(menuRow) && selectedIndex != i) {
                selectedIndex = i;
                AudioManager.playSelect();
            }
            if (game.input().buttonJustReleased(menuRow)) {
                selectedIndex = i;
                return activateSelected();
            }
        }

        if ("SOUND".equals(menuOptions[selectedIndex]) && game.input().menuLeftJustPressed()) {
            changeVolume(-0.25f);
        } else if ("SOUND".equals(menuOptions[selectedIndex]) && game.input().menuRightJustPressed()) {
            changeVolume(0.25f);
        } else if (game.input().menuDownJustPressed()) {
            selectedIndex = (selectedIndex + 1) % menuOptions.length;
            AudioManager.playSelect();
        } else if (game.input().menuUpJustPressed()) {
            selectedIndex = Math.floorMod(selectedIndex - 1, menuOptions.length);
            AudioManager.playSelect();
        } else if (game.input().confirmJustPressed()) {
            return activateSelected();
        } else if (game.input().backJustPressed()) {
            com.badlogic.gdx.Gdx.app.exit();
        }
        return false;
    }

    private boolean activateSelected() {
        if ("SOUND".equals(menuOptions[selectedIndex])) {
            AudioManager.toggleMute();
            return false;
        }
        AudioManager.playConfirm();
        switch (menuOptions[selectedIndex]) {
            case "PLAY" -> transitioning = true;
            case "DIVE SHOP" -> {
                game.showStore();
                return true;
            }
            case "ACHIEVEMENTS" -> {
                game.showAchievements();
                return true;
            }
            case "OPTIONS" -> {
                game.openOptions();
                return true;
            }
            case "CONTROLS" -> {
                game.openControls();
                return true;
            }
            case "EXIT" -> com.badlogic.gdx.Gdx.app.exit();
            default -> throw new IllegalStateException("Unknown menu option: " + menuOptions[selectedIndex]);
        }
        return false;
    }

    private Rectangle rowBounds(int index) {
        float baseline = FIRST_ROW_Y - index * ROW_SPACING;
        if (index == 3) {
            return menuRow.set(489f, baseline - 36f, 302f, 56f);
        }
        return menuRow.set(405f, baseline - 36f, 470f, 56f);
    }

    private void changeVolume(float amount) {
        AudioManager.setMasterVolume(AudioManager.getMasterVolume() + amount);
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        drawAt(font, text, GameConfig.WORLD_WIDTH / 2f, y, color);
    }

    private void drawAt(BitmapFont font, String text, float centerX, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, centerX - layout.width / 2f, y);
    }
}
