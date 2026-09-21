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

/** In-game credits and an honest summary of repository legal records. */
public final class AboutScreen extends BaseScreen {
    private static final String[][] PAGES = {
        {
            "CREDITS",
            "PROJECT BLUE: DEEP DRIFT  v" + GameConfig.VERSION,
            "Blueborn Games",
            "Support: ykucukcinar@gmail.com",
            "",
            "ART",
            "Underwater Diving art by Luis Zuno (Ansimuz)",
            "Released under Creative Commons Zero 1.0",
            "",
            "FONT",
            "Orbitron by The Orbitron Project Authors",
            "Used under the SIL Open Font License 1.1",
            "",
            "BOSS ART",
            "AI-generated project asset; repository provenance is recorded",
            "Provider and distribution terms still require release review"
        },
        {
            "NOTICES",
            "AUDIO RIGHTS",
            "Music and sound source or purchase records are incomplete",
            "Audio is not marked as cleared for production distribution",
            "",
            "SOFTWARE",
            "Built with libGDX, LWJGL, AndroidX, Kotlin and related libraries",
            "License inventory and notice requirements accompany the game",
            "",
            "ASSET RECORDS",
            "CC0 sprite and background sources are retained with hashes",
            "Generated icons and store art have deterministic build records",
            "See the distributed third-party notices for details"
        },
        {
            "PRIVACY & RELEASE STATUS",
            "Android includes Google ads and Google's consent form",
            "No standalone analytics, billing, account or crash-reporting SDK",
            "Google ads process network, device, interaction and diagnostics data",
            "Game progress and settings stay locally on the device",
            "Privacy disclosures must accompany any production store listing",
            "",
            "RELEASE REVIEW",
            "Audio commercial-rights records: incomplete",
            "AI boss provider and distribution terms: manual verification",
            "Physical-device accessibility and safe areas: manual verification",
            "",
            "These notices describe current records and do not make",
            "unsupported ownership or legal-compliance claims"
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
        drawCentered(largeFont, "ABOUT / LEGAL", 700f, Color.WHITE);
        drawPage();
        drawCentered(smallFont, "PAGE " + (page + 1) + " / " + PAGES.length,
            91f, Color.CYAN);
        drawUiButtonLabel(smallFont, "< PREV", previousButton, Color.LIGHT_GRAY);
        drawUiButtonLabel(smallFont, "BACK", backButton, Color.WHITE);
        drawUiButtonLabel(smallFont, "NEXT >", nextButton, Color.LIGHT_GRAY);
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
            String line = lines[i];
            if (line.isEmpty()) {
                y -= 14f;
                continue;
            }
            boolean heading = i == 0 || line.equals("ART") || line.equals("FONT")
                || line.equals("BOSS ART") || line.equals("AUDIO RIGHTS")
                || line.equals("SOFTWARE") || line.equals("ASSET RECORDS")
                || line.equals("RELEASE REVIEW");
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
