package com.game.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.game.diver.*;
import com.game.enemies.*;
import com.game.manager.AudioManager;
import com.game.manager.FontManager;

import java.util.*;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;
    private BitmapFont customFont;

    private Background background;
    private Diver diver;

    private ArrayList<EnemyFish> enemies;
    private ArrayList<Harpoon> harpoons;
    private ArrayList<OxygenTank> oxygenTanks;

    private float enemySpawnTimer = 0f;
    private float oxygenTankSpawnTimer = 0f;

    private float oxygen = 100f;
    private final float maxOxygen = 100f;
    private final float oxygenDecreaseRate = 5f;

    private int score = 0;
    private static int maxScore = 0;
    private int currentLevel = 1;
    private boolean gameOver = false;

    private float breathTimer = 0f;
    private final float breathInterval = 10f;

    private boolean showTutorial = true;
    private float tutorialTimer = 0f;
    private final float tutorialDuration = 4f;

    private final Random random = new Random();
    private boolean paused = false;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        customFont = FontManager.getMediumFont();

        background = new Background();
        diver = new Diver();
        enemies = new ArrayList<>();
        harpoons = new ArrayList<>();
        oxygenTanks = new ArrayList<>();

        score = 0;
        oxygen = maxOxygen;
        gameOver = false;

        AudioManager.playBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
        }

        if (!paused && !gameOver) {
            updateGame(delta);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            show();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            showTutorial = true;
            tutorialTimer = 0f;
        }

        batch.begin();

        background.render(batch);
        diver.render(batch);
        enemies.forEach(e -> e.render(batch));
        harpoons.forEach(h -> h.render(batch));
        oxygenTanks.forEach(t -> t.render(batch));

        drawHUD();

        if (showTutorial && tutorialTimer <= tutorialDuration) {
            drawTutorial();
            tutorialTimer += delta;
        } else {
            showTutorial = false;
        }

        if (gameOver) {
            layout.setText(customFont, "GAME OVER");
            customFont.setColor(Color.WHITE);
            customFont.draw(batch, layout, centerX(layout), 260);
            layout.setText(customFont, "Press R to play again");
            customFont.draw(batch, layout, centerX(layout), 230);
        }

        batch.end();

        if (paused) {
            layout.setText(customFont, "PAUSED");
            customFont.setColor(Color.YELLOW);
            batch.begin();
            customFont.draw(batch, layout, centerX(layout), 220);
            batch.end();
        }

        drawOxygenBar();
    }

    private void updateGame(float delta) {
        oxygen -= oxygenDecreaseRate * delta;
        if (oxygen <= 0) {
            oxygen = 0;
            gameOver = true;
            AudioManager.playGameOver();
        }

        breathTimer += delta;
        if (breathTimer >= breathInterval) {
            breathTimer = 0;
            AudioManager.playBreath();
        }

        score += delta * 100;
        if (score > maxScore) maxScore = score;

        checkLevelProgression();

        enemySpawnTimer += delta;
        if (enemySpawnTimer > 2f) {
            enemySpawnTimer = 0;
            spawnEnemy();
        }

        oxygenTankSpawnTimer += delta;
        if (oxygenTankSpawnTimer > 8f) {
            oxygenTankSpawnTimer = 0;
            oxygenTanks.add(new OxygenTank(Gdx.graphics.getWidth(), randomY()));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            harpoons.add(new Harpoon(diver.getPosition().x + 48, diver.getPosition().y + 20));
            AudioManager.playShoot();
        }

        harpoons.removeIf(Harpoon::isOutOfScreen);
        harpoons.forEach(h -> h.update(delta));

        enemies.removeIf(EnemyFish::isOutOfScreen);
        enemies.forEach(e -> e.update(delta));

        oxygenTanks.removeIf(OxygenTank::isOutOfScreen);
        oxygenTanks.forEach(t -> t.update(delta));

        for (Iterator<Harpoon> hIter = harpoons.iterator(); hIter.hasNext(); ) {
            Harpoon h = hIter.next();
            for (Iterator<EnemyFish> eIter = enemies.iterator(); eIter.hasNext(); ) {
                EnemyFish f = eIter.next();
                if (h.getBounds().overlaps(f.getBounds())) {
                    hIter.remove();
                    eIter.remove();
                    score += getEnemyScore(f);
                    oxygen = Math.min(maxOxygen, oxygen + 10);
                    AudioManager.playHit();
                    break;
                }
            }
        }

        for (Iterator<OxygenTank> tIter = oxygenTanks.iterator(); tIter.hasNext(); ) {
            OxygenTank t = tIter.next();
            if (t.getBounds().overlaps(diver.getBounds())) {
                oxygen = Math.min(maxOxygen, oxygen + 30);
                AudioManager.playOxygen();
                tIter.remove();
            }
        }

        for (EnemyFish f : enemies) {
            if (f.getBounds().overlaps(diver.getBounds())) {
                gameOver = true;
                AudioManager.playGameOver();
                break;
            }
        }

        diver.update(delta);
        background.update(delta);
    }

    private void checkLevelProgression() {
        if (score > 2500) currentLevel = 3;
        else if (score > 1000) currentLevel = 2;
        else currentLevel = 1;
    }

    private int getEnemyScore(EnemyFish f) {
        if (f instanceof SmallFish) return 100;
        if (f instanceof FastFish) return 150;
        if (f instanceof PiranhaSwarm) return 200;
        if (f instanceof Shark) return 300;
        return 50;
    }

    private void spawnEnemy() {
        float y = randomY();

        switch (currentLevel) {
            case 1 -> enemies.add(new SmallFish(y));
            case 2 -> {
                if (random.nextBoolean()) {
                    enemies.add(new SmallFish(y));
                } else {
                    enemies.add(new FastFish(y));
                }
            }
            case 3 -> {
                int r = random.nextInt(4);
                switch (r) {
                    case 0 -> enemies.add(new Shark(y));
                    case 1 -> enemies.add(new PiranhaSwarm(y));
                    default -> {
                        if (random.nextBoolean()) {
                            enemies.add(new SmallFish(y));
                        } else {
                            enemies.add(new FastFish(y));
                        }
                    }
                }
            }
        }
    }



    private float randomY() {
        float margin = 64f;
        return margin + random.nextFloat() * (Gdx.graphics.getHeight() - 2 * margin);
    }

    private void drawHUD() {
        customFont.setColor(Color.WHITE);
        customFont.draw(batch, "Score: " + (int) score, 10, Gdx.graphics.getHeight() - 10);
        customFont.draw(batch, "Max Score: " + (int) maxScore, 10, Gdx.graphics.getHeight() - 30);
        customFont.draw(batch, "Level: " + currentLevel, 10, Gdx.graphics.getHeight() - 50);
    }

    private void drawOxygenBar() {
        float x = 10, y = Gdx.graphics.getHeight() - 80;
        float width = 160, height = 16;

        float ratio = oxygen / maxOxygen;
        float currentWidth = ratio * width;
        float r = Math.min(1f, (1f - ratio) * 2);
        float g = Math.min(1f, ratio * 2);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(r, g, 0, 1);
        shapeRenderer.rect(x, y, currentWidth, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        layout.setText(customFont, "OXYGEN");
        customFont.setColor(Color.WHITE);
        batch.begin();
        customFont.draw(batch, layout, x + (width - layout.width) / 2, y + height - 2);
        batch.end();
    }

    private void drawTutorial() {
        float boxWidth = 460, boxHeight = 50;
        float boxX = (Gdx.graphics.getWidth() - boxWidth) / 2;
        float boxY = Gdx.graphics.getHeight() - 220;

        // Draw the semi-transparent black box (with shapeRenderer)
        batch.end(); // ← Ekle: shapeRenderer'dan önce batch kapatılmalı
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        batch.begin(); // ← Tekrar başlat

        // Draw tutorial text with batch
        customFont.setColor(Color.WHITE);
        layout.setText(customFont, "Z: Harpoon | R: Restart | ESC: Exit | T: Tutorial");
        customFont.draw(batch, layout, centerX(layout), boxY + boxHeight / 2 + layout.height / 2);
    }


    private float centerX(GlyphLayout layout) {
        return (Gdx.graphics.getWidth() - layout.width) / 2f;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        customFont.dispose();
        background.dispose();
        diver.dispose();
        enemies.forEach(EnemyFish::dispose);
        harpoons.forEach(Harpoon::dispose);
        oxygenTanks.forEach(OxygenTank::dispose);
        AudioManager.dispose();
        FontManager.dispose();
    }
}
