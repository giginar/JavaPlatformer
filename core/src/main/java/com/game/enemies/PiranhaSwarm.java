package com.game.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class PiranhaSwarm implements EnemyFish {

    private Texture texture;
    private ArrayList<Vector2> positions;
    private float speed = 200f;
    private float width = 32f;
    private float height = 24f;
    private float animationTime = 0f;
    private float baseY;

    public PiranhaSwarm(float baseY) {
        this.texture = new Texture("enemy_piranha.png");
        this.positions = new ArrayList<>();
        Random random = new Random();

        int count = 3 + random.nextInt(3);
        float spacing = 40f;
        float baseX = Gdx.graphics.getWidth() + 50;

        for (int i = 0; i < count; i++) {
            float yOffset = random.nextFloat() * 40 - 20;
            float x = baseX + i * spacing;
            float y = baseY + yOffset;
            positions.add(new Vector2(x, y));
        }
    }

    @Override
    public void update(float delta) {
        animationTime += delta;
        for (Vector2 pos : positions) {
            pos.x -= speed * delta;
            pos.y = baseY + (float)Math.sin(animationTime * 4f + pos.x * 0.02f) * 5f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        for (Vector2 pos : positions) {
            batch.draw(texture, pos.x, pos.y, width, height);
        }
    }

    @Override
    public boolean isOutOfScreen() {
        for (Vector2 pos : positions) {
            if (pos.x + width > 0) return false;
        }
        return true;
    }

    @Override
    public Rectangle getBounds() {
        if (positions.isEmpty()) return new Rectangle(0, 0, 0, 0);
        Vector2 first = positions.get(0);
        return new Rectangle(first.x, first.y, width, height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
