package com.game.diver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.game.enemies.EnemyFish;
import com.game.enemies.FastFish;
import com.game.enemies.PiranhaSwarm;
import com.game.enemies.Shark;
import com.game.enemies.SmallFish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Diver diver;
    private Background background;
    private BitmapFont font;
    private GlyphLayout layout;

    private ArrayList<EnemyFish> enemies;
    private ArrayList<Harpoon> harpoons;
    private float enemySpawnTimer = 0f;
    private float enemySpawnInterval = 2f;
    private Random random;

    private int score = 0;
    private static int maxScore = 0;
    private int currentLevel = 1;
    private boolean gameOver = false;

    private float oxygen = 100f;
    private final float maxOxygen = 100f;
    private final float oxygenDecreaseRate = 10f;
    private ArrayList<OxygenTank> oxygenTanks = new ArrayList<>();
    private float oxygenTankSpawnTimer = 0f;
    private final float oxygenTankSpawnInterval = 8f;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        diver = new Diver();
        background = new Background();
        font = new BitmapFont();
        font.getData().setScale(0.8f);
        layout = new GlyphLayout();

        enemies = new ArrayList<>();
        harpoons = new ArrayList<>();
        random = new Random();

        score = 0;
        oxygen = maxOxygen;
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        if (!gameOver) {
            updateGame(delta);
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                show(); // Restart game
            }
        }

        Gdx.gl.glClearColor(0, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        background.render(batch);
        diver.render(batch);
        for (EnemyFish f : enemies) f.render(batch);
        for (Harpoon h : harpoons) h.render(batch);
        for (OxygenTank tank : oxygenTanks) tank.render(batch);

        float hudTop = Gdx.graphics.getHeight();
        font.draw(batch, "Score: " + (int) score, 10, hudTop - 10);
        font.draw(batch, "Max Score: " + maxScore, 10, hudTop - 30);
        font.draw(batch, "Level: " + currentLevel, 10, hudTop - 50);

        float barX = 10;
        float barY = hudTop - 75;
        float barWidth = 160;
        float barHeight = 16;

        // Draw game over message
        if (gameOver) {
            layout.setText(font, "GAME OVER");
            float x = (Gdx.graphics.getWidth() - layout.width) / 2;
            float y = 260;
            font.draw(batch, layout, x, y);

            layout.setText(font, "Press R to play again");
            x = (Gdx.graphics.getWidth() - layout.width) / 2;
            y = 230;
            font.draw(batch, layout, x, y);
        }

        layout.setText(font, "OXYGEN");
        float textX = barX + (barWidth - layout.width) / 2;
        float textY = barY + (barHeight + layout.height) / 2 - 2;
        font.setColor(0, 0, 0, 1);
        font.draw(batch, layout, textX, textY);
        font.setColor(1, 1, 1, 1);

        batch.end();

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
    }

    private void updateGame(float delta) {
        oxygen -= oxygenDecreaseRate * delta;
        if (oxygen <= 0) {
            oxygen = 0;
            gameOver = true;
        }

        score += delta * 100;
        if (score > maxScore) maxScore = (int) score;

        checkLevelProgression();

        enemySpawnTimer += delta;
        if (enemySpawnTimer >= enemySpawnInterval) {
            enemySpawnTimer = 0f;
            spawnEnemy();
        }

        oxygenTankSpawnTimer += delta;
        if (oxygenTankSpawnTimer >= oxygenTankSpawnInterval) {
            oxygenTankSpawnTimer = 0f;
            float y = random.nextFloat() * (Gdx.graphics.getHeight() - 64);
            oxygenTanks.add(new OxygenTank(Gdx.graphics.getWidth(), y));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            harpoons.add(new Harpoon(diver.getPosition().x + 48, diver.getPosition().y + 20));
        }

        Iterator<Harpoon> hIter = harpoons.iterator();
        while (hIter.hasNext()) {
            Harpoon h = hIter.next();
            h.update(delta);
            if (h.isOutOfScreen()) {
                hIter.remove();
                continue;
            }

            Iterator<EnemyFish> fIter = enemies.iterator();
            while (fIter.hasNext()) {
                EnemyFish f = fIter.next();
                if (h.getBounds().overlaps(f.getBounds())) {
                    fIter.remove();
                    hIter.remove();
                    score += getEnemyScore(f);
                    oxygen = Math.min(maxOxygen, oxygen + 10);
                    break;
                }
            }
        }

        Iterator<EnemyFish> eIter = enemies.iterator();
        while (eIter.hasNext()) {
            EnemyFish f = eIter.next();
            f.update(delta);
            if (f.isOutOfScreen()) eIter.remove();
        }

        Iterator<OxygenTank> tIter = oxygenTanks.iterator();
        while (tIter.hasNext()) {
            OxygenTank tank = tIter.next();
            tank.update(delta);
            if (tank.isOutOfScreen()) {
                tIter.remove();
                continue;
            }

            if (tank.getBounds().overlaps(diver.getBounds())) {
                oxygen = Math.min(maxOxygen, oxygen + 30);
                tIter.remove();
            }
        }

        for (EnemyFish f : enemies) {
            if (f.getBounds().overlaps(diver.getBounds())) {
                gameOver = true;
                break;
            }
        }

        diver.update(delta);
        background.update(delta);
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

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        diver.dispose();
        background.dispose();
        font.dispose();
        for (EnemyFish f : enemies) f.dispose();
        for (Harpoon h : harpoons) h.dispose();
    }
}
