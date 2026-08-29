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
import com.game.manager.GameAssets;
import com.game.model.DiverSuit;
import com.game.model.PermanentUpgrade;
import com.game.model.ProgressionStore;

public final class StoreScreen extends BaseScreen {
    private enum StoreTab {
        EQUIPMENT,
        DIVE_SUITS
    }

    private static final PermanentUpgrade[] UPGRADES = PermanentUpgrade.values();
    private static final DiverSuit[] SUITS = DiverSuit.values();
    private static final Color PANEL_COLOR = new Color(0.005f, 0.03f, 0.075f, 0.9f);
    private static final Color ROW_COLOR = new Color(0.02f, 0.09f, 0.15f, 0.92f);
    private static final Color SELECTED_ROW_COLOR = new Color(0.05f, 0.28f, 0.34f, 0.98f);
    private static final Color SELECTED_TAB_COLOR = new Color(0.07f, 0.36f, 0.43f, 0.98f);
    private static final Color TAB_COLOR = new Color(0.02f, 0.13f, 0.2f, 0.96f);
    private static final Color BLACK_SUIT_COLOR = new Color(0.16f, 0.17f, 0.22f, 1f);

    private final Background background = new Background(20f);
    private final BitmapFont smallFont = FontManager.getSmallFont();
    private final BitmapFont mediumFont = FontManager.getMediumFont();
    private final BitmapFont largeFont = FontManager.getLargeFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle row = new Rectangle();
    private final Rectangle equipmentTab = new Rectangle(180f, 573f, 450f, 42f);
    private final Rectangle suitsTab = new Rectangle(650f, 573f, 450f, 42f);
    private final Rectangle backButton = new Rectangle(485f, 18f, 310f, 48f);
    private final ProgressionStore progression;

    private StoreTab activeTab = StoreTab.EQUIPMENT;
    private int selected;
    private String status = "DISTANCE EARNS 1 PEARL PER 100 M";
    private float statusTimer;

    public StoreScreen(DeepDiveDrift game) {
        super(game);
        progression = new ProgressionStore(Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME));
        if (Boolean.getBoolean("deepdive.openSuits")) {
            activeTab = StoreTab.DIVE_SUITS;
        }
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
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.rect(150f, 78f, 980f, 552f);
        drawTabShapes();
        for (int i = 0; i < itemCount(); i++) {
            rowBounds(i);
            shapeRenderer.setColor(i == selected ? SELECTED_ROW_COLOR : ROW_COLOR);
            shapeRenderer.rect(row.x, row.y, row.width, row.height);
            if (activeTab == StoreTab.DIVE_SUITS) {
                shapeRenderer.setColor(suitColor(SUITS[i]));
                shapeRenderer.rect(row.x, row.y, 7f, row.height);
            }
        }
        shapeRenderer.setColor(game.input().pointerOver(backButton)
            ? new Color(0.08f, 0.46f, 0.53f, 0.98f)
            : new Color(0.02f, 0.16f, 0.23f, 0.96f));
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.95f);
        shapeRenderer.rect(150f, 625f, 980f, 5f);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "DIVE SHOP", 704f, Color.WHITE);
        drawCentered(mediumFont, "PRESSURE PEARLS  " + progression.pearls(), 655f, Color.GOLD);
        drawTabs();
        for (int i = 0; i < itemCount(); i++) {
            if (activeTab == StoreTab.EQUIPMENT) {
                drawUpgradeRow(i);
            } else {
                drawSuitRow(i);
            }
        }
        drawCentered(smallFont, status, 128f,
            statusTimer > 0f ? Color.YELLOW : Color.LIGHT_GRAY);
        drawCentered(smallFont, inputHint(), 99f, Color.LIGHT_GRAY);
        drawCentered(smallFont, "BACK TO MENU", 51f, Color.WHITE);
        batch.end();
    }

    private void drawTabShapes() {
        shapeRenderer.setColor(activeTab == StoreTab.EQUIPMENT
            ? SELECTED_TAB_COLOR : TAB_COLOR);
        shapeRenderer.rect(equipmentTab.x, equipmentTab.y, equipmentTab.width, equipmentTab.height);
        shapeRenderer.setColor(activeTab == StoreTab.DIVE_SUITS
            ? SELECTED_TAB_COLOR : TAB_COLOR);
        shapeRenderer.rect(suitsTab.x, suitsTab.y, suitsTab.width, suitsTab.height);
    }

    /** @return true when this screen was replaced and rendering must stop immediately. */
    private boolean handleInput() {
        if (game.input().pointerJustPressed(equipmentTab)) {
            changeTab(StoreTab.EQUIPMENT);
            return false;
        }
        if (game.input().pointerJustPressed(suitsTab)) {
            changeTab(StoreTab.DIVE_SUITS);
            return false;
        }

        for (int i = 0; i < itemCount(); i++) {
            rowBounds(i);
            if (game.input().pointerOver(row)) {
                selected = i;
            }
            if (game.input().pointerJustPressed(row)) {
                selected = i;
                activateSelected();
                return false;
            }
        }
        if (game.input().pointerJustPressed(backButton)) {
            returnToMenu();
            return true;
        }
        if (game.input().menuLeftJustPressed() || game.input().menuRightJustPressed()) {
            changeTab(activeTab == StoreTab.EQUIPMENT
                ? StoreTab.DIVE_SUITS : StoreTab.EQUIPMENT);
        } else if (game.input().menuUpJustPressed()) {
            selected = Math.floorMod(selected - 1, itemCount());
            AudioManager.playSelect();
        } else if (game.input().menuDownJustPressed()) {
            selected = (selected + 1) % itemCount();
            AudioManager.playSelect();
        } else if (game.input().confirmJustPressed()) {
            activateSelected();
        } else if (game.input().backJustPressed()) {
            returnToMenu();
            return true;
        }
        return false;
    }

    private void changeTab(StoreTab tab) {
        if (activeTab == tab) {
            return;
        }
        activeTab = tab;
        selected = 0;
        status = tab == StoreTab.DIVE_SUITS
            ? "UNLOCK A SUIT, THEN EQUIP IT FOR YOUR NEXT DIVE"
            : "PERMANENT EQUIPMENT APPLIES TO EVERY SUIT";
        statusTimer = 0f;
        AudioManager.playSelect();
    }

    private void returnToMenu() {
        AudioManager.playSelect();
        game.showMainMenu();
    }

    private void activateSelected() {
        if (activeTab == StoreTab.EQUIPMENT) {
            purchaseSelectedUpgrade();
        } else {
            purchaseOrEquipSelectedSuit();
        }
    }

    private void purchaseSelectedUpgrade() {
        PermanentUpgrade upgrade = UPGRADES[selected];
        if (progression.level(upgrade) >= upgrade.maxLevel()) {
            showStatus("ALREADY AT MAX LEVEL", false);
        } else if (progression.purchase(upgrade)) {
            showStatus(upgrade.title() + " INSTALLED", true);
        } else {
            showStatus("NOT ENOUGH PRESSURE PEARLS", false);
        }
    }

    private void purchaseOrEquipSelectedSuit() {
        DiverSuit suit = SUITS[selected];
        if (progression.isSuitUnlocked(suit)) {
            if (progression.selectedSuit() == suit) {
                showStatus(suit.title() + " IS ALREADY EQUIPPED", false);
            } else {
                progression.selectSuit(suit);
                showStatus(suit.title() + " EQUIPPED", true);
            }
        } else if (progression.purchaseSuit(suit)) {
            showStatus(suit.title() + " UNLOCKED + EQUIPPED", true);
        } else {
            showStatus("NOT ENOUGH PRESSURE PEARLS", false);
        }
    }

    private void showStatus(String message, boolean confirmed) {
        status = message;
        statusTimer = 2f;
        if (confirmed) {
            AudioManager.playConfirm();
        } else {
            AudioManager.playSelect();
        }
    }

    private void drawTabs() {
        drawCenteredAt(smallFont, "EQUIPMENT", equipmentTab.x + equipmentTab.width / 2f,
            602f, activeTab == StoreTab.EQUIPMENT ? Color.YELLOW : Color.LIGHT_GRAY);
        drawCenteredAt(smallFont, "DIVE SUITS  " + unlockedSuitCount() + "/" + SUITS.length,
            suitsTab.x + suitsTab.width / 2f, 602f,
            activeTab == StoreTab.DIVE_SUITS ? Color.YELLOW : Color.LIGHT_GRAY);
    }

    private void drawUpgradeRow(int index) {
        PermanentUpgrade upgrade = UPGRADES[index];
        int level = progression.level(upgrade);
        rowBounds(index);
        Color titleColor = index == selected ? Color.YELLOW : Color.WHITE;
        smallFont.setColor(titleColor);
        smallFont.draw(batch, upgrade.title(), 205f, row.y + 52f);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, upgrade.description(), 205f, row.y + 24f);
        String price = level >= upgrade.maxLevel() ? "MAX"
            : progression.cost(upgrade) + " PEARLS";
        smallFont.setColor(level >= upgrade.maxLevel() ? Color.CYAN : Color.GOLD);
        smallFont.draw(batch, "LV " + level + "/" + upgrade.maxLevel() + "   " + price,
            850f, row.y + 38f);
    }

    private void drawSuitRow(int index) {
        DiverSuit suit = SUITS[index];
        rowBounds(index);
        boolean unlocked = progression.isSuitUnlocked(suit);
        boolean equipped = progression.selectedSuit() == suit;

        batch.setColor(suit == DiverSuit.ABYSS_BLACK
            ? new Color(0.62f, 0.65f, 0.75f, 1f) : Color.WHITE);
        batch.draw(GameAssets.texture(suit.texturePath()), 194f, row.y + 4f, 64f, 64f);
        batch.setColor(Color.WHITE);

        smallFont.setColor(index == selected ? Color.YELLOW : suitTextColor(suit));
        smallFont.draw(batch, suit.title(), 285f, row.y + 52f);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, suit.description(), 285f, row.y + 24f);

        String state = equipped ? "EQUIPPED"
            : unlocked ? "OWNED  |  EQUIP"
            : suit.cost() + " PEARLS";
        smallFont.setColor(equipped ? Color.CYAN : unlocked ? Color.LIME : Color.GOLD);
        smallFont.draw(batch, state, 865f, row.y + 38f);
    }

    private int unlockedSuitCount() {
        int count = 0;
        for (DiverSuit suit : SUITS) {
            if (progression.isSuitUnlocked(suit)) {
                count++;
            }
        }
        return count;
    }

    private int itemCount() {
        return activeTab == StoreTab.EQUIPMENT ? UPGRADES.length : SUITS.length;
    }

    private Rectangle rowBounds(int index) {
        return row.set(180f, 486f - index * 82f, 920f, 72f);
    }

    private String inputHint() {
        if (game.input().usingController()) {
            return "GAMEPAD  LEFT/RIGHT TAB  |  D-PAD CHOOSE  |  [A] BUY/EQUIP  |  [B] BACK";
        }
        if (game.input().usingTouch()) {
            return "TAP A TAB, THEN TAP AN ITEM TO BUY OR EQUIP";
        }
        return "KEYBOARD  LEFT/RIGHT TAB  |  UP/DOWN CHOOSE  |  [ENTER/SPACE] BUY/EQUIP  |  [ESC] BACK";
    }

    private Color suitColor(DiverSuit suit) {
        return switch (suit) {
            case TIDELINE_BLUE -> Color.CYAN;
            case SALVAGE_GREEN -> Color.LIME;
            case RESCUE_RED -> Color.SCARLET;
            case ABYSS_BLACK -> BLACK_SUIT_COLOR;
        };
    }

    private Color suitTextColor(DiverSuit suit) {
        return suit == DiverSuit.ABYSS_BLACK ? Color.LIGHT_GRAY : suitColor(suit);
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        drawCenteredAt(font, text, GameConfig.WORLD_WIDTH / 2f, y, color);
    }

    private void drawCenteredAt(BitmapFont font, String text, float centerX, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, centerX - layout.width / 2f, y);
    }
}
