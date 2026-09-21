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
import com.game.i18n.Localization;
import com.game.model.DiverSuit;
import com.game.model.PermanentUpgrade;
import com.game.model.ProgressionStore;

import java.util.Locale;

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
    private String status = Localization.text("store.distance_hint");
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
        shapeRenderer.rect(150f, 78f, 980f, 614f);
        shapeRenderer.setColor(0.08f, 0.12f, 0.16f, 1f);
        shapeRenderer.rect(756f, 625f, 344f, 54f);
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(756f, 625f, 3f, 54f);
        drawTabShapes();
        for (int i = 0; i < itemCount(); i++) {
            rowBounds(i);
            shapeRenderer.setColor(i == selected ? SELECTED_ROW_COLOR : ROW_COLOR);
            drawUiButton(row, shapeRenderer.getColor(), i == selected);
            if (activeTab == StoreTab.DIVE_SUITS) {
                shapeRenderer.setColor(suitColor(SUITS[i]));
                shapeRenderer.rect(row.x, row.y, 7f, row.height);
            } else {
                drawUpgradeProgress(UPGRADES[i]);
            }
        }
        drawUiButton(backButton, false);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.95f);
        shapeRenderer.rect(150f, 690f, 980f, 2f);
        endShapes();

        batch.begin();
        largeFont.setColor(Color.WHITE);
        largeFont.draw(batch, Localization.text("store.title"), 180f, 672f);
        drawCenteredAt(mediumFont, Localization.text("store.balance", progression.pearls()), 928f, 663f, Color.GOLD);
        drawTabs();
        for (int i = 0; i < itemCount(); i++) {
            if (activeTab == StoreTab.EQUIPMENT) {
                drawUpgradeRow(i);
            } else {
                drawSuitRow(i);
            }
        }
        String defaultStatus = activeTab == StoreTab.EQUIPMENT
            ? Localization.text("store.install_times") : Localization.text("store.suit_hint");
        drawCentered(smallFont, statusTimer > 0f ? status : defaultStatus, 128f,
            statusTimer > 0f ? Color.YELLOW : Color.LIGHT_GRAY);
        if (activeTab == StoreTab.EQUIPMENT) {
            drawCentered(smallFont, Localization.text("store.offline_hint"),
                102f, Color.LIGHT_GRAY);
        }
        drawUiButtonLabel(smallFont, Localization.text("common.back_to_menu"), backButton, Color.WHITE);
        batch.end();
    }

    private void drawTabShapes() {
        shapeRenderer.setColor(activeTab == StoreTab.EQUIPMENT
            ? SELECTED_TAB_COLOR : TAB_COLOR);
        drawUiButton(equipmentTab, shapeRenderer.getColor(), activeTab == StoreTab.EQUIPMENT);
        shapeRenderer.setColor(activeTab == StoreTab.DIVE_SUITS
            ? SELECTED_TAB_COLOR : TAB_COLOR);
        drawUiButton(suitsTab, shapeRenderer.getColor(), activeTab == StoreTab.DIVE_SUITS);
    }

    /** @return true when this screen was replaced and rendering must stop immediately. */
    private boolean handleInput() {
        if (game.input().buttonJustReleased(equipmentTab)) {
            changeTab(StoreTab.EQUIPMENT);
            return false;
        }
        if (game.input().buttonJustReleased(suitsTab)) {
            changeTab(StoreTab.DIVE_SUITS);
            return false;
        }

        for (int i = 0; i < itemCount(); i++) {
            rowBounds(i);
            if (game.input().pointerOver(row)) {
                selected = i;
            }
            if (game.input().buttonJustReleased(row)) {
                selected = i;
                activateSelected();
                return false;
            }
        }
        if (game.input().buttonJustReleased(backButton)) {
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
            ? Localization.text("store.suit_hint") : Localization.text("store.equipment_hint");
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
        long remaining = progression.remainingInstallMillis(upgrade);
        if (remaining > 0L) {
            showStatus(Localization.text("store.install_left", progression.level(upgrade) + 1,
                formatDuration(remaining)), false);
        } else if (progression.level(upgrade) >= upgrade.maxLevel()) {
            showStatus(Localization.text("store.already_max"), false);
        } else if (progression.purchase(upgrade)) {
            showStatus(Localization.text("store.install_started", upgrade.title(),
                progression.level(upgrade) + 1), true);
        } else {
            showStatus(Localization.text("store.not_enough"), false);
        }
    }

    private void purchaseOrEquipSelectedSuit() {
        DiverSuit suit = SUITS[selected];
        if (progression.isSuitUnlocked(suit)) {
            if (progression.selectedSuit() == suit) {
                showStatus(Localization.text("store.already_equipped", suit.title()), false);
            } else {
                progression.selectSuit(suit);
                showStatus(Localization.text("store.equipped_status", suit.title()), true);
            }
        } else if (progression.purchaseSuit(suit)) {
            showStatus(Localization.text("store.unlocked_equipped", suit.title()), true);
        } else {
            showStatus(Localization.text("store.not_enough"), false);
        }
    }

    private void showStatus(String message, boolean confirmed) {
        status = message;
        statusTimer = 4f;
        if (confirmed) {
            AudioManager.playConfirm();
        } else {
            AudioManager.playSelect();
        }
    }

    private void drawTabs() {
        drawUiButtonLabel(smallFont, Localization.text("store.equipment"), equipmentTab,
            activeTab == StoreTab.EQUIPMENT ? Color.YELLOW : Color.LIGHT_GRAY);
        drawUiButtonLabel(smallFont, Localization.text("store.suits", unlockedSuitCount(), SUITS.length), suitsTab,
            activeTab == StoreTab.DIVE_SUITS ? Color.YELLOW : Color.LIGHT_GRAY);
    }

    private void drawUpgradeRow(int index) {
        PermanentUpgrade upgrade = UPGRADES[index];
        long remaining = progression.remainingInstallMillis(upgrade);
        int level = progression.level(upgrade);
        rowBounds(index);
        Color titleColor = index == selected ? Color.YELLOW : Color.WHITE;
        smallFont.setColor(titleColor);
        drawFittedText(upgrade.title() + "  " + Localization.text("common.level") + " " + level + "/" + upgrade.maxLevel(),
            205f, row.y + 52f, 450f, false);
        smallFont.setColor(Color.LIGHT_GRAY);
        drawFittedText(upgrade.description(), 205f, row.y + 24f, 450f, false);
        String action;
        String detail;
        if (remaining > 0L) {
            action = Localization.text("store.installing", level + 1);
            detail = Localization.text("store.ready_in", formatDuration(remaining));
            smallFont.setColor(Color.GOLD);
        } else if (level >= upgrade.maxLevel()) {
            action = Localization.text("store.max_level");
            detail = Localization.text("store.ready_next");
            smallFont.setColor(Color.CYAN);
        } else {
            action = Localization.text("store.buy", level + 1, progression.cost(upgrade));
            detail = Localization.text("store.install", formatDuration(upgrade.installDurationMillis(level + 1)));
            smallFont.setColor(progression.pearls() >= progression.cost(upgrade)
                ? Color.GOLD : Color.LIGHT_GRAY);
        }
        drawFittedText(action, 1080f, row.y + 52f, 270f, true);
        drawFittedText(detail, 1080f, row.y + 27f, 270f, true);
    }

    private void drawUpgradeProgress(PermanentUpgrade upgrade) {
        long remaining = progression.remainingInstallMillis(upgrade);
        int level = progression.level(upgrade);
        for (int slot = 0; slot < upgrade.maxLevel(); slot++) {
            shapeRenderer.setColor(slot < level ? Color.CYAN
                : remaining > 0L && slot == level ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(680f + slot * 38f, row.y + 30f, 26f, 12f);
        }
        if (remaining > 0L && level < upgrade.maxLevel()) {
            float progress = 1f - Math.min(1f,
                (float) remaining / upgrade.installDurationMillis(level + 1));
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(810f, row.y + 8f, 270f, 3f);
            shapeRenderer.setColor(Color.GOLD);
            shapeRenderer.rect(810f, row.y + 8f, 270f * progress, 3f);
        }
    }

    private static String formatDuration(long milliseconds) {
        long seconds = (milliseconds + 999L) / 1000L;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60L, seconds % 60L);
    }

    private void drawFittedText(String text, float x, float y, float maxWidth, boolean rightAligned) {
        float scaleX = smallFont.getData().scaleX;
        float scaleY = smallFont.getData().scaleY;
        layout.setText(smallFont, text);
        if (layout.width > maxWidth) {
            float ratio = maxWidth / layout.width;
            smallFont.getData().setScale(scaleX * ratio, scaleY * ratio);
            layout.setText(smallFont, text);
        }
        smallFont.draw(batch, layout, rightAligned ? x - layout.width : x, y);
        smallFont.getData().setScale(scaleX, scaleY);
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

        String state = equipped ? Localization.text("store.equipped")
            : unlocked ? Localization.text("store.owned_equip")
            : suit.cost() + " " + Localization.text("common.pearls");
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
