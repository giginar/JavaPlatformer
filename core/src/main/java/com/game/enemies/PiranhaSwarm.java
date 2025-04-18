package com.game.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Random;

public class PiranhaSwarm implements EnemyFish {

    private final Texture texture;
    private final ArrayList<Vector2> positions;
    private final float speed = 200f;
    private float speedMultiplier = 1f;
    private final float width = 32f;
    private final float height = 24f;

    private final Rectangle bounds;
    private int hp = 3;
    private float hitEffectTimer = 0f;

    public PiranhaSwarm(float baseY) {
        this.texture = new Texture("enemy_piranha.png");
        this.positions = new ArrayList<>();

        Random random = new Random();
        int count = 3 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            float yOffset = random.nextFloat() * 40 - 20;
            positions.add(new Vector2(Gdx.graphics.getWidth() + i * 10, baseY + yOffset));
        }

        Vector2 first = positions.get(0);
        this.bounds = new Rectangle(first.x, first.y, width, height);
    }

    @Override
    public void update(float delta) {
        for (Vector2 pos : positions) {
            pos.x -= speed * delta * speedMultiplier;;
        }

        if (hitEffectTimer > 0) {
            hitEffectTimer -= delta;
        }

        Vector2 first = positions.get(0);
        bounds.setPosition(first.x, first.y);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitEffectTimer > 0) {
            batch.setColor(Color.RED);
        }

        for (Vector2 pos : positions) {
            batch.draw(texture, pos.x, pos.y, width, height);
        }

        if (hitEffectTimer > 0) {
            batch.setColor(Color.WHITE);
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
        return bounds;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    @Override
    public void hit() {
        hp--;
        hitEffectTimer = 0.2f;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    @Override
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }
}
