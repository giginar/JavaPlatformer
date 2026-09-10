package com.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.game.DeepDiveDrift;
import com.game.GameConfig;
import com.game.diver.Background;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;
import com.game.model.ChallengeModifier;
import com.game.model.RunDifficulty;
import com.game.model.RunSettings;

import java.util.EnumSet;
import java.util.Locale;

public final class DiveSetupScreen extends BaseScreen {
    private static final String DIFFICULTY_KEY = "runSetup.difficulty";
    private static final String CHALLENGE_KEY_PREFIX = "runSetup.challenge.";
    private static final RunDifficulty[] DIFFICULTIES = RunDifficulty.values();
    private static final ChallengeModifier[] MODIFIERS = ChallengeModifier.values();

    private final Background background = new Background(22f);
    private final BitmapFont smallFont = FontManager.getSmallFont();
    private final BitmapFont mediumFont = FontManager.getMediumFont();
    private final BitmapFont largeFont = FontManager.getLargeFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle interactive = new Rectangle();
    private final Rectangle startButton = new Rectangle(370f, 24f, 540f, 56f);
    private final Rectangle backButton = new Rectangle(950f, 24f, 200f, 56f);
    private final Preferences preferences;
    private final EnumSet<ChallengeModifier> modifiers = EnumSet.noneOf(ChallengeModifier.class);

    private RunDifficulty difficulty;
    private int selectedControl;

    public DiveSetupScreen(DeepDiveDrift game) {
        super(game);
        preferences = Gdx.app.getPreferences(GameConfig.PREFERENCES_NAME);
        difficulty = savedDifficulty();
        for (ChallengeModifier modifier : MODIFIERS) {
            if (preferences.getBoolean(CHALLENGE_KEY_PREFIX + modifier.name(), false)) {
                modifiers.add(modifier);
            }
        }
        background.setDepthStage(2, 0.8f);
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 0.1f);
        prepareFrame(0f, 0.04f, 0.1f);
        updateInput();
        background.update(frameDelta);
        if (handleInput()) {
            return;
        }

        batch.begin();
        background.render(batch);
        batch.end();

        beginFilledShapes();
        shapeRenderer.setColor(0.005f, 0.025f, 0.065f, 0.94f);
        shapeRenderer.rect(135f, 92f, 1010f, 548f);
        shapeRenderer.setColor(0.1f, 0.75f, 0.9f, 0.95f);
        shapeRenderer.rect(135f, 635f, 1010f, 5f);
        drawDifficultyShapes();
        drawChallengeShapes();
        drawUiButton(startButton, selectedControl == 5);
        drawUiButton(backButton, selectedControl == 6);
        endShapes();

        batch.begin();
        drawCentered(largeFont, "PREPARE YOUR DIVE", 704f, Color.WHITE);
        drawCentered(smallFont, "CHOOSE A DIFFICULTY AND OPTIONAL CHALLENGE RULES",
            658f, Color.LIGHT_GRAY);
        drawCentered(smallFont, "DIFFICULTY", 608f, Color.CYAN);
        drawDifficulties();
        drawCentered(smallFont, "CHALLENGE MODE MODIFIERS", 468f,
            modifiers.isEmpty() ? Color.LIGHT_GRAY : Color.YELLOW);
        drawChallenges();

        RunSettings settings = currentSettings();
        String reward = String.format(Locale.ROOT, "PEARL REWARD  x%.2f",
            settings.rewardMultiplier());
        drawCentered(smallFont, reward, 110f, Color.GOLD);
        if (settings.hasAllChallenges()) {
            drawCentered(smallFont, "TOTAL LOCKDOWN ACTIVE  |  SPECIAL ACHIEVEMENTS AVAILABLE",
                87f, Color.SCARLET);
        }
        drawUiButtonLabel(mediumFont, "START DIVE", startButton, Color.WHITE);
        drawUiButtonLabel(smallFont, "BACK", backButton, Color.LIGHT_GRAY);
        batch.end();
    }

    private void drawDifficultyShapes() {
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            difficultyBounds(i);
            RunDifficulty option = DIFFICULTIES[i];
            boolean active = difficulty == option;
            shapeRenderer.setColor(active
                ? new Color(0.06f, 0.32f, 0.39f, 0.98f)
                : new Color(0.015f, 0.09f, 0.15f, 0.94f));
            drawUiButton(interactive, shapeRenderer.getColor(), active);
            if (active) {
                shapeRenderer.setColor(difficultyColor(option));
                shapeRenderer.rect(interactive.x, interactive.y + interactive.height - 2f,
                    interactive.width, 2f);
            }
        }
    }

    private void drawChallengeShapes() {
        for (int i = 0; i < MODIFIERS.length; i++) {
            modifierBounds(i);
            boolean enabled = modifiers.contains(MODIFIERS[i]);
            shapeRenderer.setColor(selectedControl == i + 1
                ? new Color(0.055f, 0.29f, 0.35f, 0.98f)
                : enabled
                ? new Color(0.08f, 0.2f, 0.23f, 0.96f)
                : new Color(0.015f, 0.075f, 0.13f, 0.94f));
            drawUiButton(interactive, shapeRenderer.getColor(), selectedControl == i + 1);
            shapeRenderer.setColor(enabled ? Color.SCARLET : Color.DARK_GRAY);
            shapeRenderer.rect(interactive.x, interactive.y, 7f, interactive.height);
        }
    }

    private boolean handleInput() {
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            difficultyBounds(i);
            if (game.input().buttonJustReleased(interactive)) {
                selectedControl = 0;
                setDifficulty(DIFFICULTIES[i]);
                return false;
            }
        }
        for (int i = 0; i < MODIFIERS.length; i++) {
            modifierBounds(i);
            if (game.input().buttonJustReleased(interactive)) {
                selectedControl = i + 1;
                toggleModifier(MODIFIERS[i]);
                return false;
            }
        }
        if (game.input().buttonJustReleased(startButton)) {
            startDive();
            return true;
        }
        if (game.input().buttonJustReleased(backButton) || game.input().backJustPressed()) {
            game.showMainMenu();
            AudioManager.playSelect();
            return true;
        }

        if (game.input().keyboardConfirmJustPressed()) {
            startDive();
            return true;
        }

        if (game.input().menuUpJustPressed()) {
            selectedControl = Math.floorMod(selectedControl - 1, 7);
            AudioManager.playSelect();
        } else if (game.input().menuDownJustPressed()) {
            selectedControl = (selectedControl + 1) % 7;
            AudioManager.playSelect();
        } else if (game.input().menuLeftJustPressed()) {
            adjustSelected(-1);
        } else if (game.input().menuRightJustPressed()) {
            adjustSelected(1);
        } else if (game.input().controllerConfirmJustPressed()) {
            if (selectedControl == 0) {
                adjustSelected(1);
            } else if (selectedControl <= MODIFIERS.length) {
                toggleModifier(MODIFIERS[selectedControl - 1]);
            } else if (selectedControl == 5) {
                startDive();
                return true;
            } else {
                game.showMainMenu();
                AudioManager.playSelect();
                return true;
            }
        }
        return false;
    }

    private void adjustSelected(int direction) {
        if (selectedControl == 0) {
            int next = Math.floorMod(difficulty.ordinal() + direction, DIFFICULTIES.length);
            setDifficulty(DIFFICULTIES[next]);
        } else if (selectedControl <= MODIFIERS.length) {
            toggleModifier(MODIFIERS[selectedControl - 1]);
        }
    }

    private void setDifficulty(RunDifficulty selected) {
        difficulty = selected;
        preferences.putString(DIFFICULTY_KEY, selected.name());
        preferences.flush();
        AudioManager.playSelect();
    }

    private void toggleModifier(ChallengeModifier modifier) {
        if (!modifiers.remove(modifier)) {
            modifiers.add(modifier);
        }
        preferences.putBoolean(CHALLENGE_KEY_PREFIX + modifier.name(), modifiers.contains(modifier));
        preferences.flush();
        AudioManager.playSelect();
    }

    private void startDive() {
        AudioManager.playConfirm();
        game.startNewGame(currentSettings());
    }

    private RunSettings currentSettings() {
        return new RunSettings(difficulty, modifiers);
    }

    private RunDifficulty savedDifficulty() {
        String saved = preferences.getString(DIFFICULTY_KEY, RunDifficulty.NORMAL.name());
        try {
            return RunDifficulty.valueOf(saved);
        } catch (IllegalArgumentException ignored) {
            return RunDifficulty.NORMAL;
        }
    }

    private void drawDifficulties() {
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            RunDifficulty option = DIFFICULTIES[i];
            difficultyBounds(i);
            drawCenteredAt(mediumFont, option.title(), interactive.x + interactive.width / 2f,
                552f, difficulty == option ? Color.YELLOW : difficultyColor(option));
            drawDifficultyDetail(difficultyDetail(option, 0),
                interactive.x + interactive.width / 2f, 522f, Color.LIGHT_GRAY);
            drawDifficultyDetail(difficultyDetail(option, 1),
                interactive.x + interactive.width / 2f, 502f, Color.LIGHT_GRAY);
        }
    }

    private void drawDifficultyDetail(String text, float centerX, float y, Color color) {
        float scaleX = smallFont.getData().scaleX;
        float scaleY = smallFont.getData().scaleY;
        layout.setText(smallFont, text);
        float ratio = Math.min(1f, 264f / layout.width);
        smallFont.getData().setScale(scaleX * ratio, scaleY * ratio);
        drawCenteredAt(smallFont, text, centerX, y, color);
        smallFont.getData().setScale(scaleX, scaleY);
    }

    private void drawChallenges() {
        for (int i = 0; i < MODIFIERS.length; i++) {
            ChallengeModifier modifier = MODIFIERS[i];
            modifierBounds(i);
            boolean enabled = modifiers.contains(modifier);
            smallFont.setColor(enabled ? Color.YELLOW : Color.LIGHT_GRAY);
            smallFont.draw(batch, (enabled ? "[ON]  " : "[OFF] ") + modifier.title(),
                260f, interactive.y + 39f);
            smallFont.setColor(Color.LIGHT_GRAY);
            smallFont.draw(batch, modifier.description(), 635f, interactive.y + 39f);
        }
    }

    private Rectangle difficultyBounds(int index) {
        return interactive.set(175f + index * 310f, 480f, 290f, 96f);
    }

    private Rectangle modifierBounds(int index) {
        return interactive.set(230f, 378f - index * 68f, 820f, 56f);
    }

    private Color difficultyColor(RunDifficulty option) {
        return switch (option) {
            case EASY -> Color.LIME;
            case NORMAL -> Color.CYAN;
            case HARD -> Color.SCARLET;
        };
    }

    private String difficultyDetail(RunDifficulty option, int line) {
        return switch (option) {
            case EASY -> line == 0 ? "SPEED -15%  DAMAGE -25%"
                : "UPGRADES +15%  AIR +15%";
            case NORMAL -> line == 0 ? "STANDARD THREATS"
                : "UPGRADES 100%  REWARD x1";
            case HARD -> line == 0 ? "SPEED +22%  DAMAGE +30%"
                : "UPGRADES -25%  AIR -12%";
        };
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
