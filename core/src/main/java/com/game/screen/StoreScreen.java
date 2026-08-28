package com.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;
import com.game.GameConfig;
import com.game.diver.Background;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.model.PermanentUpgrade;
import com.game.model.ProgressionStore;

public final class StoreScreen extends BaseScreen {
    private static final PermanentUpgrade[] UPGRADES = PermanentUpgrade.values();
    private final Background background = new Background(20f);
    private final BitmapFont smallFont = FontManager.getSmallFont();
    private final BitmapFont mediumFont = FontManager.getMediumFont();
    private final BitmapFont largeFont = FontManager.getLargeFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle row = new Rectangle();
    private final Rectangle backButton = new Rectangle(485f, 18f, 310f, 48f);
    private final ProgressionStore progression;
    private int selected;
    private String status = "DISTANCE EARNS 1 PEARL PER 100 M";
    private float statusTimer;

    public StoreScreen(DeepDiveDrift game) {
        super(game);
        progression = new ProgressionStore(Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME));
        background.setDepthStage(3, 0.7f);
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 0.1f);
        prepareFrame(0f, 0.035f, 0.09f);
        updateInput();
        background.update(frameDelta);
        statusTimer = Math.max(0f, statusTimer - frameDelta);
        if (handleInput()) {
            return;
        }

        batch.begin();
        background.render(batch);
        batch.end();

        beginFilledShapes();
        shapeRenderer.setColor(0.005f, 0.03f, 0.075f, 0.9f);
        shapeRenderer.rect(150f, 90f, 980f, 555f);
        for (int i = 0; i < UPGRADES.length; i++) {
            rowBounds(i);
            shapeRenderer.setColor(i == selected
                ? new Color(0.05f, 0.28f, 0.34f, 0.98f)
                : new Color(0.02f, 0.09f, 0.15f, 0.92f));
            shapeRenderer.rect(row.x, row.y, row.width, row.height);
        }
        shapeRenderer.setColor(game.input().pointerOver(backButton)
            ? new Color(0.08f, 0.46f, 0.53f, 0.98f)
            : new Color(0.02f, 0.16f, 0.23f, 0.96f));
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.95f);
        shapeRenderer.rect(150f, 640f, 980f, 5f);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "DIVE SHOP", 690f, Color.WHITE);
        drawCentered(mediumFont, "PRESSURE PEARLS  " + progression.pearls(), 625f, Color.GOLD);
        for (int i = 0; i < UPGRADES.length; i++) {
            drawUpgradeRow(i);
        }
        drawCentered(smallFont, status, 114f,
            statusTimer > 0f ? Color.YELLOW : Color.LIGHT_GRAY);
        drawCentered(smallFont, inputHint(), 88f, Color.LIGHT_GRAY);
        drawCentered(smallFont, "BACK TO MENU", 51f, Color.WHITE);
        batch.end();
    }

    /**
     * @return {@code true} when this screen was replaced and rendering must stop immediately
     */
    private boolean handleInput() {
        for (int i = 0; i < UPGRADES.length; i++) {
            rowBounds(i);
            if (game.input().pointerOver(row)) {
                selected = i;
            }
            if (game.input().pointerJustPressed(row)) {
                selected = i;
                purchaseSelected();
                return false;
            }
        }
        if (game.input().pointerJustPressed(backButton)) {
            returnToMenu();
            return true;
        }
        if (game.input().menuUpJustPressed()) {
            selected = Math.floorMod(selected - 1, UPGRADES.length);
            AudioManager.playSelect();
        } else if (game.input().menuDownJustPressed()) {
            selected = (selected + 1) % UPGRADES.length;
            AudioManager.playSelect();
        } else if (game.input().confirmJustPressed()) {
            purchaseSelected();
        } else if (game.input().backJustPressed()) {
            returnToMenu();
            return true;
        }
        return false;
    }

    private void returnToMenu() {
        AudioManager.playSelect();
        game.showMainMenu();
    }

    private void purchaseSelected() {
        PermanentUpgrade upgrade = UPGRADES[selected];
        if (progression.level(upgrade) >= upgrade.maxLevel()) {
            status = "ALREADY AT MAX LEVEL";
            statusTimer = 2f;
            AudioManager.playSelect();
        } else if (progression.purchase(upgrade)) {
            status = upgrade.title() + " INSTALLED";
            statusTimer = 2f;
            AudioManager.playConfirm();
        } else {
            status = "NOT ENOUGH PRESSURE PEARLS";
            statusTimer = 2f;
            AudioManager.playSelect();
        }
    }

    private void drawUpgradeRow(int index) {
        PermanentUpgrade upgrade = UPGRADES[index];
        int level = progression.level(upgrade);
        float y = 548f - index * 88f;
        Color titleColor = index == selected ? Color.YELLOW : Color.WHITE;
        smallFont.setColor(titleColor);
        smallFont.draw(batch, upgrade.title(), 205f, y);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, upgrade.description(), 205f, y - 28f);
        String price = level >= upgrade.maxLevel() ? "MAX"
            : progression.cost(upgrade) + " PEARLS";
        smallFont.setColor(level >= upgrade.maxLevel() ? Color.CYAN : Color.GOLD);
        smallFont.draw(batch, "LV " + level + "/" + upgrade.maxLevel() + "   " + price,
            850f, y - 12f);
    }

    private Rectangle rowBounds(int index) {
        return row.set(180f, 489f - index * 88f, 920f, 76f);
    }

    private String inputHint() {
        if (game.input().usingController()) {
            return "GAMEPAD  D-PAD CHOOSE  |  [A] BUY  |  [B] BACK";
        }
        if (game.input().usingTouch()) {
            return "TAP AN UPGRADE TO BUY";
        }
        return "KEYBOARD  UP / DOWN OR W-S CHOOSE  |  [ENTER / SPACE] BUY  |  [ESC] BACK";
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
    }
}
