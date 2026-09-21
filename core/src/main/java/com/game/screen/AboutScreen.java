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
import com.game.i18n.Localization;

/** In-game credits and an honest summary of repository legal records. */
public final class AboutScreen extends BaseScreen {
    private static final String[][] PAGES = {
        {
            "about.credits",
            "brand.version",
            "brand.studio",
            "about.support",
            "",
            "about.art", "about.art.credit", "about.art.license",
            "",
            "about.font", "about.font.credit", "about.font.license",
            "",
            "about.boss_art", "about.boss.line1", "about.boss.line2"
        },
        {
            "about.notices", "about.audio", "about.audio.line1", "about.audio.line2",
            "",
            "about.software", "about.software.line1", "about.software.line2",
            "",
            "about.assets", "about.assets.line1", "about.assets.line2", "about.assets.line3"
        },
        {
            "about.privacy", "about.privacy.line1", "about.privacy.line2",
            "about.privacy.line3", "about.privacy.line4", "about.privacy.line5",
            "",
            "about.release", "about.release.line1", "about.release.line2", "about.release.line3",
            "",
            "about.release.line4", "about.release.line5"
        }
    };

    private final Background background = new Background(12f);
    private final BitmapFont smallFont = FontManager.getSmallFont();
    private final BitmapFont mediumFont = FontManager.getMediumFont();
    private final BitmapFont largeFont = FontManager.getLargeFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle previousButton = new Rectangle(170f, 18f, 180f, 48f);
    private final Rectangle backButton = new Rectangle(485f, 18f, 310f, 48f);
    private final Rectangle nextButton = new Rectangle(930f, 18f, 180f, 48f);
    private int page;
    private int selected = 2;

    public AboutScreen(DeepDiveDrift game) {
        super(game);
        background.setDepthStage(4, 0.65f);
    }

    @Override
    public void render(float delta) {
        prepareFrame(0f, 0.025f, 0.075f);
        updateInput();
        if (handleInput()) return;
        background.update(Math.min(delta, 0.1f));

        batch.begin();
        background.render(batch);
        batch.end();

        beginFilledShapes();
        shapeRenderer.setColor(0.005f, 0.025f, 0.065f, 0.96f);
        shapeRenderer.rect(145f, 82f, 990f, 555f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.95f);
        shapeRenderer.rect(145f, 632f, 990f, 5f);
        drawUiButton(previousButton, selected == 0);
        drawUiButton(backButton, selected == 2);
        drawUiButton(nextButton, selected == 1);
        endShapes();

        batch.begin();
        drawCentered(largeFont, Localization.text("about.title"), 700f, Color.WHITE);
        drawPage();
        drawCentered(smallFont, Localization.text("common.page", page + 1, PAGES.length),
            91f, Color.CYAN);
        drawUiButtonLabel(smallFont, Localization.text("common.previous"), previousButton, Color.LIGHT_GRAY);
        drawUiButtonLabel(smallFont, Localization.text("common.back"), backButton, Color.WHITE);
        drawUiButtonLabel(smallFont, Localization.text("common.next"), nextButton, Color.LIGHT_GRAY);
        batch.end();
    }

    private boolean handleInput() {
        if (game.input().buttonJustReleased(previousButton)) {
            changePage(-1);
        } else if (game.input().buttonJustReleased(nextButton)) {
            changePage(1);
        } else if (game.input().buttonJustReleased(backButton) || game.input().backJustPressed()) {
            AudioManager.playSelect();
            game.closeOverlay();
            return true;
        } else if (game.input().menuLeftJustPressed()) {
            selected = 0;
            changePage(-1);
        } else if (game.input().menuRightJustPressed()) {
            selected = 1;
            changePage(1);
        } else if (game.input().menuUpJustPressed() || game.input().menuDownJustPressed()) {
            selected = selected == 2 ? 0 : 2;
            AudioManager.playSelect();
        } else if (game.input().confirmJustPressed()) {
            if (selected == 0) changePage(-1);
            else if (selected == 1) changePage(1);
            else {
                AudioManager.playConfirm();
                game.closeOverlay();
                return true;
            }
        }
        return false;
    }

    private void changePage(int direction) {
        page = Math.floorMod(page + direction, PAGES.length);
        AudioManager.playSelect();
    }

    private void drawPage() {
        String[] lines = PAGES[page];
        float y = 586f;
        for (int i = 0; i < lines.length; i++) {
            String key = lines[i];
            if (key.isEmpty()) {
                y -= 14f;
                continue;
            }
            boolean heading = i == 0 || key.equals("about.art") || key.equals("about.font")
                || key.equals("about.boss_art") || key.equals("about.audio")
                || key.equals("about.software") || key.equals("about.assets")
                || key.equals("about.release");
            String line = switch (key) {
                case "brand.version" -> "PROJECT BLUE: DEEP DRIFT  v" + GameConfig.VERSION;
                case "brand.studio" -> "Blueborn Games";
                default -> Localization.text(key);
            };
            drawCentered(heading ? mediumFont : smallFont, line, y,
                heading ? Color.CYAN : Color.LIGHT_GRAY);
            y -= heading ? 42f : 30f;
        }
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        float originalX = font.getData().scaleX;
        float originalY = font.getData().scaleY;
        if (layout.width > 900f) {
            float fit = 900f / layout.width;
            font.getData().setScale(originalX * fit, originalY * fit);
            layout.setText(font, text);
        }
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
        font.getData().setScale(originalX, originalY);
    }
}
