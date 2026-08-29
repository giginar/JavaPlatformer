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
import com.game.model.Achievement;
import com.game.model.AchievementStore;

public final class AchievementScreen extends BaseScreen {
    private static final int ROWS_PER_PAGE = 8;
    private static final Achievement[] ACHIEVEMENTS = Achievement.values();

    private final Background background = new Background(18f);
    private final BitmapFont smallFont = FontManager.getSmallFont();
    private final BitmapFont mediumFont = FontManager.getMediumFont();
    private final BitmapFont largeFont = FontManager.getLargeFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle row = new Rectangle();
    private final Rectangle previousPageButton = new Rectangle(170f, 18f, 180f, 48f);
    private final Rectangle backButton = new Rectangle(485f, 18f, 310f, 48f);
    private final Rectangle nextPageButton = new Rectangle(930f, 18f, 180f, 48f);
    private final AchievementStore achievements;
    private int selected;

    public AchievementScreen(DeepDiveDrift game) {
        super(game);
        achievements = new AchievementStore(
            Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME));
        background.setDepthStage(4, 0.65f);
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 0.1f);
        prepareFrame(0f, 0.025f, 0.075f);
        updateInput();
        background.update(frameDelta);
        if (handleInput()) {
            return;
        }

        batch.begin();
        background.render(batch);
        batch.end();

        int first = page() * ROWS_PER_PAGE;
        int visible = Math.min(ROWS_PER_PAGE, ACHIEVEMENTS.length - first);
        beginFilledShapes();
        shapeRenderer.setColor(0.005f, 0.025f, 0.065f, 0.94f);
        shapeRenderer.rect(145f, 82f, 990f, 555f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.95f);
        shapeRenderer.rect(145f, 632f, 990f, 5f);
        for (int i = 0; i < visible; i++) {
            int achievementIndex = first + i;
            rowBounds(i);
            boolean unlocked = achievements.isUnlocked(ACHIEVEMENTS[achievementIndex]);
            shapeRenderer.setColor(achievementIndex == selected
                ? new Color(0.055f, 0.29f, 0.35f, 0.98f)
                : unlocked
                ? new Color(0.035f, 0.15f, 0.18f, 0.95f)
                : new Color(0.012f, 0.065f, 0.115f, 0.94f));
            shapeRenderer.rect(row.x, row.y, row.width, row.height);
            shapeRenderer.setColor(unlocked ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(row.x, row.y, 7f, row.height);
        }
        shapeRenderer.setColor(0.02f, 0.16f, 0.23f, 0.96f);
        shapeRenderer.rect(previousPageButton.x, previousPageButton.y,
            previousPageButton.width, previousPageButton.height);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        shapeRenderer.rect(nextPageButton.x, nextPageButton.y,
            nextPageButton.width, nextPageButton.height);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "ACHIEVEMENTS", 700f, Color.WHITE);
        drawCentered(mediumFont, achievements.unlockedCount() + " / " + ACHIEVEMENTS.length
            + " UNLOCKED", 651f, Color.GOLD);
        for (int i = 0; i < visible; i++) {
            drawAchievement(first + i, i);
        }
        drawCentered(smallFont, "PAGE " + (page() + 1) + " / " + pageCount(),
            91f, Color.CYAN);
        drawCentered(smallFont, inputHint(), 72f, Color.LIGHT_GRAY);
        drawCenteredAt(smallFont, "< PREV", previousPageButton.x
            + previousPageButton.width / 2f, 51f, Color.LIGHT_GRAY);
        drawCentered(smallFont, "BACK TO MENU", 51f, Color.WHITE);
        drawCenteredAt(smallFont, "NEXT >", nextPageButton.x
            + nextPageButton.width / 2f, 51f, Color.LIGHT_GRAY);
        batch.end();
    }

    private boolean handleInput() {
        int first = page() * ROWS_PER_PAGE;
        int visible = Math.min(ROWS_PER_PAGE, ACHIEVEMENTS.length - first);
        for (int i = 0; i < visible; i++) {
            rowBounds(i);
            if (game.input().pointerOver(row)) {
                selected = first + i;
            }
        }
        if (game.input().pointerJustPressed(previousPageButton)) {
            changePage(-1);
            return false;
        }
        if (game.input().pointerJustPressed(nextPageButton)) {
            changePage(1);
            return false;
        }
        if (game.input().pointerJustPressed(backButton) || game.input().backJustPressed()) {
            AudioManager.playSelect();
            game.showMainMenu();
            return true;
        }
        if (game.input().menuUpJustPressed()) {
            selected = Math.floorMod(selected - 1, ACHIEVEMENTS.length);
            AudioManager.playSelect();
        } else if (game.input().menuDownJustPressed()) {
            selected = (selected + 1) % ACHIEVEMENTS.length;
            AudioManager.playSelect();
        } else if (game.input().menuLeftJustPressed()) {
            changePage(-1);
        } else if (game.input().menuRightJustPressed()) {
            changePage(1);
        }
        return false;
    }

    private void changePage(int direction) {
        int rowWithinPage = selected % ROWS_PER_PAGE;
        int nextPage = Math.floorMod(page() + direction, pageCount());
        selected = Math.min(ACHIEVEMENTS.length - 1,
            nextPage * ROWS_PER_PAGE + rowWithinPage);
        AudioManager.playSelect();
    }

    private void drawAchievement(int achievementIndex, int visibleRow) {
        Achievement achievement = ACHIEVEMENTS[achievementIndex];
        boolean unlocked = achievements.isUnlocked(achievement);
        rowBounds(visibleRow);
        smallFont.setColor(unlocked ? Color.GOLD
            : achievementIndex == selected ? Color.YELLOW : Color.LIGHT_GRAY);
        smallFont.draw(batch, String.format("%02d  %s", achievementIndex + 1,
            achievement.title()), 185f, row.y + 43f);
        smallFont.setColor(unlocked ? Color.WHITE : Color.GRAY);
        smallFont.draw(batch, achievement.description(), 500f, row.y + 43f);
        smallFont.setColor(unlocked ? Color.LIME : Color.DARK_GRAY);
        smallFont.draw(batch, achievements.progressText(achievement), 990f, row.y + 43f);
    }

    private int page() {
        return selected / ROWS_PER_PAGE;
    }

    private int pageCount() {
        return (ACHIEVEMENTS.length + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
    }

    private Rectangle rowBounds(int visibleRow) {
        return row.set(170f, 548f - visibleRow * 57f, 940f, 50f);
    }

    private String inputHint() {
        if (game.input().usingController()) {
            return "D-PAD BROWSE  |  [B] BACK";
        }
        if (game.input().usingTouch()) {
            return "TAP PREV / NEXT TO BROWSE";
        }
        return "UP / DOWN BROWSE  |  LEFT / RIGHT PAGE  |  ESC BACK";
    }

    private void drawCentered(BitmapFont font, String text, float y, Color color) {
        drawCenteredAt(font, text, GameConfig.WORLD_WIDTH / 2f, y, color);
    }

    private void drawCenteredAt(BitmapFont font, String text, float centerX, float y,
                                Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, centerX - layout.width / 2f, y);
    }
}
