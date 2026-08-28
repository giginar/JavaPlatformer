package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;
import com.game.manager.GameAssets;

/** Ranged controller: holds a distant lane, telegraphs, then fires aimed electric bolts. */
public final class ElectricEel implements EnemyFish {
    private static final float WIDTH = 82f;
    private static final float HEIGHT = 34f;
    private final Texture texture = GameAssets.texture(GameAssets.FAST_FISH);
    private final Vector2 position;
    private final Rectangle bounds;
    private float speedMultiplier = 1f;
    private float shotTimer = 1.7f;
    private float animationTime;
    private float hitTimer;
    private boolean shotReady;
    private int hp = 2;

    public ElectricEel(float y) {
        position = new Vector2(GameConfig.WORLD_WIDTH + 20f, y);
        bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        hitTimer = Math.max(0f, hitTimer - delta);
        if (position.x > 900f) {
            position.x -= 135f * speedMultiplier * delta;
        } else {
            position.x -= 24f * speedMultiplier * delta;
            position.y = MathUtils.lerp(position.y, targetY - HEIGHT / 2f,
                Math.min(1f, delta * 0.75f));
        }
        shotTimer -= delta;
        if (shotTimer <= 0f && position.x < 1050f) {
            shotReady = true;
            shotTimer = MathUtils.random(2.2f, 3.1f) / speedMultiplier;
        }
        bounds.setPosition(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        float glow = 0.55f + MathUtils.sin(animationTime * 11f) * 0.3f;
        if (hitTimer > 0f) {
            batch.setColor(Color.RED);
        } else {
            batch.setColor(0.25f, glow, 1f, 1f);
        }
        batch.draw(texture, position.x, position.y, WIDTH, HEIGHT);
        batch.setColor(Color.WHITE);
    }

    @Override
    public EnemyProjectile pollProjectile(float targetX, float targetY) {
        if (!shotReady) {
            return null;
        }
        shotReady = false;
        return new EnemyProjectile(position.x, position.y + HEIGHT / 2f,
            targetX, targetY, 285f * speedMultiplier);
    }

    @Override public boolean isOutOfScreen() { return position.x + WIDTH < 0f; }
    @Override public Rectangle getBounds() { return bounds; }
    @Override public void hit() { hp--; hitTimer = 0.18f; }
    @Override public boolean isDead() { return hp <= 0; }
    @Override public void setSpeedMultiplier(float speed) { speedMultiplier = speed; }
    @Override public int getScoreValue() { return 240; }
    @Override public float getCollisionDamage() { return 28f; }
    @Override public boolean isTelegraphing() { return shotTimer < 0.55f; }
    @Override public float getHealthRatio() { return Math.max(0f, hp / 2f); }
}
