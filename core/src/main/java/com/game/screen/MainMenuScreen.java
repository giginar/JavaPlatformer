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

public class MainMenuScreen extends BaseScreen {
    private static final String[] MENU_OPTIONS = {"PLAY", "OPTIONS", "EXIT"};
    private static final float TRANSITION_DURATION = 0.45f;
    private static final Color ACCENT_COLOR = new Color(0.55f, 0.9f, 1f, 1f);

    private final Background background;
    private final BitmapFont smallFont;
    private final BitmapFont mediumFont;
    private final BitmapFont largeFont;
    private final GlyphLayout layout;

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
    }

    @Override
    public void show() {
        AudioManager.playBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 0.1f);
        prepareFrame(0f, 0.05f, 0.12f);

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
        shapeRenderer.rect(405f, 205f, 470f, 300f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.9f);
        shapeRenderer.rect(405f, 500f, 470f, 5f);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "DEEP DIVE DRIFT", 610f, Color.WHITE);
        drawCentered(smallFont, "SURVIVE THE ABYSS", 557f, ACCENT_COLOR);

        float startY = 420f;
        for (int i = 0; i < MENU_OPTIONS.length; i++) {
            Color color = i == selectedIndex ? Color.YELLOW : Color.LIGHT_GRAY;
            String prefix = i == selectedIndex ? ">  " : "   ";
            drawCentered(mediumFont, prefix + MENU_OPTIONS[i], startY - i * 62f, color);
        }

        drawCentered(smallFont, "ARROW KEYS TO NAVIGATE  |  ENTER TO SELECT", 72f, Color.LIGHT_GRAY);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % MENU_OPTIONS.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + MENU_OPTIONS.length) % MENU_OPTIONS.length;
            AudioManager.playSelect();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            AudioManager.playConfirm();
            switch (selectedIndex) {
                case 0 -> transitioning = true;
                case 1 -> {
                    game.openOptions();
                    return true;
                }
                case 2 -> Gdx.app.exit();
                default -> throw new IllegalStateException("Unknown menu option: " + selectedIndex);
            }
        }
        return false;
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }
}
