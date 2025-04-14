package com.game.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    public PiranhaSwarm(float baseY) {
        this.texture = new Texture("enemy_piranha.png");
        this.positions = new ArrayList<>();
        Random random = new Random();

        // Generate a small group of 3–5 piranhas
        int count = 3 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            float yOffset = random.nextFloat() * 40 - 20; // slight vertical variation
            positions.add(new Vector2(800 + i * 10, baseY + yOffset));
        }
    }

    @Override
    public void update(float delta) {
        for (Vector2 pos : positions) {
            pos.x -= speed * delta;
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
        // Return one bounding box for simplicity (can be expanded later)
        if (positions.isEmpty()) return new Rectangle(0, 0, 0, 0);
        Vector2 first = positions.get(0);
        return new Rectangle(first.x, first.y, width, height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
