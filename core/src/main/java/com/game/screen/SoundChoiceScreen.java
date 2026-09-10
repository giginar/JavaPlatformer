package com.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;
import com.game.diver.Background;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;

/** The first mobile launch stays silent until the player makes an explicit choice. */
public final class SoundChoiceScreen extends BaseScreen {
    private static final Rectangle SILENT_BUTTON = new Rectangle(310f, 254f, 320f, 96f);
    private static final Rectangle SOUND_BUTTON = new Rectangle(650f, 254f, 320f, 96f);
    private final Background background = new Background(24f);
    private final GlyphLayout layout = new GlyphLayout();
    private boolean soundSelected;

    public SoundChoiceScreen(DeepDiveDrift game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        prepareFrame(0f, 0.05f, 0.12f);
        updateInput();
        if (game.input().buttonJustReleased(SILENT_BUTTON) || game.input().backJustPressed()) {
            choose(false);
            return;
        }
        if (game.input().buttonJustReleased(SOUND_BUTTON)) {
            choose(true);
            return;
        }
        if (game.input().menuLeftJustPressed() || game.input().menuUpJustPressed()) {
            soundSelected = false;
        } else if (game.input().menuRightJustPressed() || game.input().menuDownJustPressed()) {
            soundSelected = true;
        } else if (game.input().confirmJustPressed()) {
            choose(soundSelected);
            return;
        }

        background.update(Math.min(delta, 0.1f));
        batch.begin();
        background.render(batch);
        batch.end();
        beginFilledShapes();
        shapeRenderer.setColor(0.01f, 0.05f, 0.11f, 0.94f);
        shapeRenderer.rect(270f, 188f, 740f, 336f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 1f);
        shapeRenderer.rect(270f, 519f, 740f, 5f);
        drawUiButton(SILENT_BUTTON, !soundSelected);
        drawUiButton(SOUND_BUTTON, soundSelected);
        endShapes();

        batch.begin();
        drawCentered(FontManager.getLargeFont(), "BEFORE YOU DIVE", 474f, 640f, Color.WHITE);
        drawCentered(FontManager.getSmallFont(), "Would you like to play with sound?", 403f, 640f, Color.LIGHT_GRAY);
        drawUiButtonLabel(FontManager.getMediumFont(), "PLAY SILENTLY", SILENT_BUTTON, Color.WHITE);
        drawUiButtonLabel(FontManager.getMediumFont(), "ENABLE SOUND", SOUND_BUTTON, Color.WHITE);
        drawCentered(FontManager.getSmallFont(), "Change sound anytime from the main menu.", 221f, 640f, Color.LIGHT_GRAY);
        batch.end();
    }

    private void choose(boolean enabled) {
        AudioManager.chooseStartupSound(enabled);
        game.showMainMenu();
    }

    private void drawCentered(BitmapFont font, String text, float y, float centerX, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, centerX - layout.width / 2f, y);
    }
}
