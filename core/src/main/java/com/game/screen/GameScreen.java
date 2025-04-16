package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.game.diver.*;
import com.game.enemies.*;

import java.util.*;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Diver diver;
    private Background background;
    private BitmapFont font;
    private GlyphLayout layout;

    private ArrayList<EnemyFish> enemies;
    private ArrayList<Harpoon> harpoons;
    private ArrayList<OxygenTank> oxygenTanks;
    private Random random;

    private float enemySpawnTimer = 0f;
    private float oxygenTankSpawnTimer = 0f;

    private int score = 0;
    private static int maxScore = 0;
    private int currentLevel = 1;
    private boolean gameOver = false;

    private float oxygen = 100f;
    private final float maxOxygen = 100f;
    private final float oxygenDecreaseRate = 5f;

    private float breathTimer = 0f;
    private final float breathInterval = 10f;

    private boolean showTutorial = true;
    private float tutorialTimer = 0f;
    private final float tutorialDisplayDuration = 5f;

    private Sound shootSound, hitSound, oxygenSound, gameoverSound, breathSound;
    private Music backgroundMusic;
    private Preferences prefs;
    private boolean musicOn, sfxOn;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(0.8f);
        layout = new GlyphLayout();
        random = new Random();

        diver = new Diver();
        background = new Background();
        enemies = new ArrayList<>();
        harpoons = new ArrayList<>();
        oxygenTanks = new ArrayList<>();

        score = 0;
        oxygen = maxOxygen;
        gameOver = false;

        prefs = Gdx.app.getPreferences("DeepDiveDriftPrefs");
        musicOn = prefs.getBoolean("musicOn", true);
        sfxOn = prefs.getBoolean("sfxOn", true);

        if (musicOn) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("underwater.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f);
            backgroundMusic.play();
        }

        shootSound = Gdx.audio.newSound(Gdx.files.internal("shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
        oxygenSound = Gdx.audio.newSound(Gdx.files.internal("oxygen.wav"));
        gameoverSound = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
        breathSound = Gdx.audio.newSound(Gdx.files.internal("breath.mp3"));
    }

    @Override
    public void render(float delta) {
        if (!gameOver) {
            updateGame(delta);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            show();
        }

        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        background.render(batch);
        diver.render(batch);
        enemies.forEach(e -> e.render(batch));
        harpoons.forEach(h -> h.render(batch));
        oxygenTanks.forEach(o -> o.render(batch));
        drawHUD();

        if (showTutorial) {
            tutorialTimer += delta;
            if (tutorialTimer <= tutorialDisplayDuration) {
                layout.setText(font, "Z = Fire, R = Restart, ESC = Quit");
                font.setColor(Color.WHITE);
                font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2, Gdx.graphics.getHeight() - 100);
            } else {
                showTutorial = false;
            }
        }

        batch.end();
        drawOxygenBar();
    }

    private void updateGame(float delta) {
        oxygen -= oxygenDecreaseRate * delta;
        if (oxygen <= 0) {
            oxygen = 0;
            gameOver = true;
        }

        breathTimer += delta;
        if (breathTimer >= breathInterval) {
            breathTimer = 0f;
            if (sfxOn && breathSound != null) breathSound.play();
        }

        score += delta * 100;
        if (score > maxScore) maxScore = (int) score;

        checkLevelProgression();

        enemySpawnTimer += delta;
        if (enemySpawnTimer >= 2f) {
            enemySpawnTimer = 0f;
            spawnEnemy();
        }

        oxygenTankSpawnTimer += delta;
        if (oxygenTankSpawnTimer >= 8f) {
            oxygenTankSpawnTimer = 0f;
            oxygenTanks.add(new OxygenTank(Gdx.graphics.getWidth(), random.nextFloat() * (Gdx.graphics.getHeight() - 64)));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            harpoons.add(new Harpoon(diver.getPosition().x + 48, diver.getPosition().y + 20));
            if (sfxOn && shootSound != null) shootSound.play();
        }

        harpoons.removeIf(h -> {
            h.update(delta);
            if (h.isOutOfScreen()) return true;
            for (Iterator<EnemyFish> fIter = enemies.iterator(); fIter.hasNext(); ) {
                EnemyFish f = fIter.next();
                if (h.getBounds().overlaps(f.getBounds())) {
                    if (sfxOn && hitSound != null) hitSound.play();
                    fIter.remove();
                    score += getEnemyScore(f);
                    oxygen = Math.min(maxOxygen, oxygen + 10);
                    return true;
                }
            }
            return false;
        });

        enemies.removeIf(f -> {
            f.update(delta);
            return f.isOutOfScreen();
        });

        oxygenTanks.removeIf(t -> {
            t.update(delta);
            if (t.isOutOfScreen()) return true;
            if (t.getBounds().overlaps(diver.getBounds())) {
                oxygen = Math.min(maxOxygen, oxygen + 30);
                if (sfxOn && oxygenSound != null) oxygenSound.play();
                return true;
            }
            return false;
        });

        for (EnemyFish f : enemies) {
            if (f.getBounds().overlaps(diver.getBounds())) {
                gameOver = true;
                if (sfxOn && gameoverSound != null) gameoverSound.play();
                break;
            }
        }

        diver.update(delta);
        background.update(delta);
    }

    private void drawHUD() {
        float hudTop = Gdx.graphics.getHeight();
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + (int) score, 10, hudTop - 10);
        font.draw(batch, "Max Score: " + maxScore, 10, hudTop - 30);
        font.draw(batch, "Level: " + currentLevel, 10, hudTop - 50);

        if (gameOver) {
            layout.setText(font, "GAME OVER");
            font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2, 260);
            layout.setText(font, "Press R to play again");
            font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2, 230);
        }
    }

    private void drawOxygenBar() {
        float barX = 10, barY = Gdx.graphics.getHeight() - 85, barWidth = 160, barHeight = 16;
        float oxygenRatio = oxygen / maxOxygen;
        float currentWidth = oxygenRatio * barWidth;
        float r = Math.min(1f, (1f - oxygenRatio) * 2);
        float g = Math.min(1f, oxygenRatio * 2);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.setColor(r, g, 0, 1);
        shapeRenderer.rect(barX, barY, currentWidth, barHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();

        batch.begin();
        layout.setText(font, "OXYGEN");
        font.setColor(Color.BLACK);
        font.draw(batch, layout, barX + (barWidth - layout.width) / 2, barY + (barHeight + layout.height) / 2 - 2);
        batch.end();
    }

    private void spawnEnemy() {
        float y = random.nextFloat() * (Gdx.graphics.getHeight() - 64);
        switch (currentLevel) {
            case 1 -> enemies.add(new SmallFish(y));
            case 2 -> enemies.add(random.nextBoolean() ? new SmallFish(y) : new FastFish(y));
            case 3 -> {
                int r = random.nextInt(4);
                if (r == 0) enemies.add(new Shark(y));
                else if (r == 1) enemies.add(new PiranhaSwarm(y));
                else enemies.add(random.nextBoolean() ? new SmallFish(y) : new FastFish(y));
            }
        }
    }

    private int getEnemyScore(EnemyFish f) {
        if (f instanceof SmallFish) return 100;
        if (f instanceof FastFish) return 150;
        if (f instanceof PiranhaSwarm) return 200;
        if (f instanceof Shark) return 300;
        return 100;
    }

    private void checkLevelProgression() {
        if (score > 2500) currentLevel = 3;
        else if (score > 1000) currentLevel = 2;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        diver.dispose();
        background.dispose();
        if (shootSound != null) shootSound.dispose();
        if (hitSound != null) hitSound.dispose();
        if (oxygenSound != null) oxygenSound.dispose();
        if (gameoverSound != null) gameoverSound.dispose();
        if (breathSound != null) breathSound.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        enemies.forEach(EnemyFish::dispose);
        harpoons.forEach(Harpoon::dispose);
        oxygenTanks.forEach(OxygenTank::dispose);
    }
}
