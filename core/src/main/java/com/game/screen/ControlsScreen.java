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

/** Shows the bindings for the input device currently in use. */
public final class ControlsScreen extends BaseScreen {
    private static final String[] ACTIONS = {
        "SWIM", "FIRE", "DASH", "PAUSE", "HELP",
        "NAVIGATE", "SELECT / BUY", "CHANGE / TABS", "BACK"
    };
    private static final String[] KEYBOARD_BINDINGS = {
        "Hold SPACE / W / UP or LEFT MOUSE",
        "Press Z / X or RIGHT MOUSE",
        "C (requires a dash charge)",
        "P / ESC",
        "T / F1 to show the starting hints again",
        "Arrow keys / W-A-S-D or mouse",
        "ENTER / SPACE or click an option",
        "LEFT / RIGHT or A / D",
        "ESC or the BACK button"
    };
    private static final String[] CONTROLLER_BINDINGS = {
        "Hold A / LEFT STICK UP / D-PAD UP",
        "X / B / RB / RT",
        "LB (requires a dash charge)",
        "START",
        "Y to show the starting hints again",
        "D-PAD / LEFT STICK",
        "A",
        "D-PAD LEFT / RIGHT",
        "B / BACK"
    };
    private static final String[] TOUCH_BINDINGS = {
        "Hold anywhere along the LEFT edge",
        "Tap anywhere along the RIGHT edge",
        "Tap DASH when a charge is available",
        "Tap the pause icon at the top right",
        "Pause > OPTIONS > CONTROLS",
        "Tap an option or a page button",
        "Tap the item to buy or equip it",
        "Tap a setting, a tab, or - / +",
        "Tap the BACK button"
    };

    private final Background background = new Background(12f);
    private final BitmapFont smallFont = FontManager.getSmallFont();
    private final BitmapFont mediumFont = FontManager.getMediumFont();
    private final BitmapFont largeFont = FontManager.getLargeFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle backButton = new Rectangle(485f, 18f, 310f, 48f);

    public ControlsScreen(DeepDiveDrift game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        prepareFrame(0f, 0.035f, 0.09f);
        updateInput();
        if (game.input().backJustPressed() || game.input().confirmJustPressed()
            || game.input().buttonJustReleased(backButton)) {
            AudioManager.playSelect();
            game.closeOverlay();
            return;
        }

        boolean controller = game.input().usingController();
        boolean touch = game.input().usingTouch();
        String[] bindings = controller ? CONTROLLER_BINDINGS : touch ? TOUCH_BINDINGS : KEYBOARD_BINDINGS;
        String device = controller ? "GAMEPAD" : touch ? "TOUCH" : "KEYBOARD & MOUSE";
        background.update(Math.min(delta, 0.1f));
        batch.begin();
        background.render(batch);
        batch.end();

        beginFilledShapes();
        shapeRenderer.setColor(0.005f, 0.025f, 0.065f, 1f);
        shapeRenderer.rect(150f, 84f, 980f, 608f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 1f);
        shapeRenderer.rect(150f, 690f, 980f, 2f);
        shapeRenderer.setColor(0.03f, 0.18f, 0.24f, 1f);
        shapeRenderer.rect(440f, 588f, 400f, 42f);
        shapeRenderer.rect(180f, 314f, 920f, 1f);
        for (int i = 0; i < ACTIONS.length; i++) {
            if (i % 2 == 0) {
                shapeRenderer.setColor(0.015f, 0.065f, 0.105f, 1f);
                shapeRenderer.rect(180f, rowY(i) - 26f, 920f, 36f);
            }
        }
        drawUiButton(backButton, false);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "CONTROLS", 672f, Color.WHITE);
        drawCentered(smallFont, device, 616f, Color.CYAN);
        mediumFont.setColor(Color.WHITE);
        mediumFont.draw(batch, "IN THE WATER", 194f, 560f);
        mediumFont.draw(batch, "MENUS & SHOP", 194f, 290f);
        for (int i = 0; i < ACTIONS.length; i++) {
            smallFont.setColor(Color.CYAN);
            smallFont.draw(batch, ACTIONS[i], 194f, rowY(i));
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, bindings[i], 460f, rowY(i));
        }
        drawUiButtonLabel(smallFont, "BACK", backButton, Color.WHITE);
        batch.end();
    }

    private float rowY(int index) {
        return index < 5 ? 518f - index * 42f : 248f - (index - 5) * 42f;
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }
}
