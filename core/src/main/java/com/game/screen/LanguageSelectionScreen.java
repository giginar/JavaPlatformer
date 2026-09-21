package com.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;
import com.game.diver.Background;
import com.game.i18n.GameLanguage;
import com.game.i18n.Localization;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;

/** First-launch language choice. No choice is persisted until explicit confirmation. */
public final class LanguageSelectionScreen extends BaseScreen {
    private static final Rectangle ENGLISH_BUTTON = new Rectangle(310f, 280f, 320f, 88f);
    private static final Rectangle TURKISH_BUTTON = new Rectangle(650f, 280f, 320f, 88f);
    private static final Rectangle CONFIRM_BUTTON = new Rectangle(485f, 185f, 310f, 62f);
    private final Background background = new Background(24f);
    private final GlyphLayout layout = new GlyphLayout();
    private GameLanguage selected = Localization.suggestedLanguage();

    public LanguageSelectionScreen(DeepDiveDrift game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        prepareFrame(0f, 0.05f, 0.12f);
        updateInput();
        if (game.input().buttonJustReleased(ENGLISH_BUTTON)) selected = GameLanguage.ENGLISH;
        if (game.input().buttonJustReleased(TURKISH_BUTTON)) selected = GameLanguage.TURKISH;
        if (game.input().menuLeftJustPressed() || game.input().menuUpJustPressed()) {
            selected = GameLanguage.ENGLISH;
            AudioManager.playSelect();
        } else if (game.input().menuRightJustPressed() || game.input().menuDownJustPressed()) {
            selected = GameLanguage.TURKISH;
            AudioManager.playSelect();
        }
        if (game.input().confirmJustPressed() || game.input().buttonJustReleased(CONFIRM_BUTTON)) {
            Localization.select(selected);
            AudioManager.playConfirm();
            game.continueStartup();
            return;
        }

        background.update(Math.min(delta, 0.1f));
        batch.begin();
        background.render(batch);
        batch.end();
        beginFilledShapes();
        shapeRenderer.setColor(0.01f, 0.05f, 0.11f, 0.96f);
        shapeRenderer.rect(270f, 150f, 740f, 390f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 1f);
        shapeRenderer.rect(270f, 535f, 740f, 5f);
        drawUiButton(ENGLISH_BUTTON, selected == GameLanguage.ENGLISH);
        drawUiButton(TURKISH_BUTTON, selected == GameLanguage.TURKISH);
        drawUiButton(CONFIRM_BUTTON, false);
        endShapes();

        batch.begin();
        drawCentered(FontManager.getLargeFont(), "CHOOSE LANGUAGE / DİL SEÇİN", 490f, Color.WHITE);
        drawUiButtonLabel(FontManager.getMediumFont(), GameLanguage.ENGLISH.nativeName(), ENGLISH_BUTTON, Color.WHITE);
        drawUiButtonLabel(FontManager.getMediumFont(), GameLanguage.TURKISH.nativeName(), TURKISH_BUTTON, Color.WHITE);
        drawUiButtonLabel(FontManager.getMediumFont(), selected == GameLanguage.TURKISH ? "ONAYLA" : "CONFIRM", CONFIRM_BUTTON, Color.WHITE);
        drawCentered(FontManager.getSmallFont(), "You can change this later in Options. / Bunu daha sonra Ayarlar'dan değiştirebilirsiniz.", 122f, Color.LIGHT_GRAY);
        batch.end();
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, 640f - layout.width / 2f, y);
    }
}
