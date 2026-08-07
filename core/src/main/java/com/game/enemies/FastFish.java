package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.game.GameConfig;
import com.game.manager.GameAssets;

public class FastFish implements EnemyFish {

    private final Texture texture;
    private final Vector2 position;
    private final Rectangle bounds;
    private float speedMultiplier = 1f;
    private float animationTime = 0f;
    private final float baseY;
    private int hp = 1;
    private float hitEffectTimer;

    private static final float SPEED = 440f;
    private static final float WIDTH = 46f;
    private static final float HEIGHT = 32f;

    public FastFish(float y) {
        this.texture = GameAssets.texture(GameAssets.FAST_FISH);
        this.position = new Vector2(GameConfig.WORLD_WIDTH, y);
        this.baseY = y;
        this.bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }

    @Override
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        position.x -= SPEED * delta * speedMultiplier;
        position.y = MathUtils.clamp(baseY + MathUtils.sin(animationTime * 7.5f) * 52f,
            10f, GameConfig.WORLD_HEIGHT - HEIGHT - 10f);
        bounds.setPosition(position.x, position.y);
        hitEffectTimer = Math.max(0f, hitEffectTimer - delta);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitEffectTimer > 0f) {
            batch.setColor(Color.RED);
        }
        float pulse = 1f + MathUtils.sin(animationTime * 12f) * 0.08f;
        batch.draw(texture, position.x, position.y - (HEIGHT * pulse - HEIGHT) / 2f,
            WIDTH / pulse, HEIGHT * pulse);
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
    public int getScoreValue() {
        return 150;
    }

    @Override
    public float getCollisionDamage() {
        return 25f;
    }
}
