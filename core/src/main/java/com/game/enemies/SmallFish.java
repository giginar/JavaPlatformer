package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.manager.GameAssets;

public class SmallFish implements EnemyFish {

    private final Texture texture;
    private final Vector2 position;
    private final Rectangle bounds;
    private float speedMultiplier = 1f;
    private float animationTime = 0f;
    private final float baseY;
    private int hp = 1;
    private float hitEffectTimer;

    private static final float SPEED = 155f;
    private static final float WIDTH = 38f;
    private static final float HEIGHT = 29f;

    public SmallFish(float y) {
        this.texture = GameAssets.texture(GameAssets.SMALL_FISH);
        this.position = new Vector2(GameConfig.WORLD_WIDTH, y);
        this.baseY = y;
        this.bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        position.x -= SPEED * speedMultiplier * delta;
        position.y = baseY + (float)Math.sin(animationTime * 4f) * 5f;
        bounds.setPosition(position.x, position.y);
        hitEffectTimer = Math.max(0f, hitEffectTimer - delta);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitEffectTimer > 0f) {
            batch.setColor(Color.RED);
        }
        float pulse = 1f + MathUtils.sin(animationTime * 7f) * 0.055f;
        float drawWidth = WIDTH * pulse;
        float drawHeight = HEIGHT / pulse;
        batch.draw(texture, position.x - (drawWidth - WIDTH) / 2f,
            position.y - (drawHeight - HEIGHT) / 2f, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    @Override
    public boolean isOutOfScreen() {
        return position.x + WIDTH < 0;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void hit() {
        hp--;
        hitEffectTimer = 0.16f;
    }

    @Override
    public boolean isDead() {
        return hp <= 0;
    }

    @Override
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public int getScoreValue() {
        return 100;
    }

    @Override
    public float getCollisionDamage() {
        return 18f;
    }
}
