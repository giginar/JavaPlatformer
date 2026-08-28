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
    private static final String[] DESKTOP_MENU_OPTIONS = {"PLAY", "DIVE SHOP", "OPTIONS", "EXIT"};
    private static final String[] MOBILE_MENU_OPTIONS = {"PLAY", "DIVE SHOP", "OPTIONS"};
    private static final float TRANSITION_DURATION = 0.45f;
    private static final Color ACCENT_COLOR = new Color(0.55f, 0.9f, 1f, 1f);

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
        shapeRenderer.setColor(0.01f, 0.05f, 0.11f, 0.78f);
        shapeRenderer.rect(405f, 175f, 470f, 330f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.9f);
        shapeRenderer.rect(405f, 500f, 470f, 5f);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "DEEP DIVE DRIFT", 610f, Color.WHITE);
        drawCentered(smallFont, "SURVIVE THE ABYSS", 557f, ACCENT_COLOR);

        float startY = 420f;
        for (int i = 0; i < menuOptions.length; i++) {
            Color color = i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY;
            String prefix = i == selectedIndex ? ">  " : "   ";
            drawCentered(mediumFont, prefix + menuOptions[i], startY - i * 62f, color);
        }

        drawCentered(smallFont, inputHint(), 72f, Color.LIGHT_GRAY);
        batch.end();

        if (transitioning) {
            beginFilledShapes();
            shapeRenderer.setColor(0f, 0f, 0f, transitionAlpha);
            shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
            endShapes();

            if (transitionAlpha >= 1f) {
                game.startNewGame();
            }
        }
    }

    private boolean handleInput() {
        for (int i = 0; i < menuOptions.length; i++) {
            rowBounds(i);
            if (game.input().pointerOver(menuRow) && selectedIndex != i) {
                selectedIndex = i;
                AudioManager.playSelect();
            }
            if (game.input().pointerJustPressed(menuRow)) {
                selectedIndex = i;
                return activateSelected();
            }
        }

        if (game.input().menuDownJustPressed()) {
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
        AudioManager.playConfirm();
        switch (menuOptions[selectedIndex]) {
            case "PLAY" -> transitioning = true;
            case "DIVE SHOP" -> {
                game.showStore();
                return true;
            }
            case "OPTIONS" -> {
                game.openOptions();
                return true;
            }
            case "EXIT" -> com.badlogic.gdx.Gdx.app.exit();
            default -> throw new IllegalStateException("Unknown menu option: " + menuOptions[selectedIndex]);
        }
        return false;
    }

    private Rectangle rowBounds(int index) {
        float baseline = 420f - index * 62f;
        return menuRow.set(405f, baseline - 40f, 470f, 55f);
    }

    private String inputHint() {
        if (game.input().usingController()) {
            return "GAMEPAD  D-PAD / STICK NAVIGATE  |  [A] SELECT  |  [B] EXIT";
        }
        if (game.input().usingTouch()) {
            return "TAP AN OPTION TO SELECT";
        }
        return "KEYBOARD  ARROWS / W-S NAVIGATE  |  [ENTER / SPACE] SELECT  |  [ESC] EXIT";
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }
}
