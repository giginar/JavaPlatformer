package com.game.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.manager.GameAssets;

/** A tiny broodling thrown from the final boss; it actively corrects toward the diver. */
public final class OctopusMinion implements EnemyFish {
    private static final float SIZE = 62f;
    private final Texture texture = GameAssets.texture(GameAssets.OCTOPUS_BOSS);
    private final Vector2 position;
    private final Rectangle bounds;
    private float animationTime;
    private float hitTimer;
    private int hp = 1;

    public OctopusMinion(float y) {
        position = new Vector2(940f, MathUtils.clamp(y, 30f, 628f));
        bounds = new Rectangle(position.x + 8f, position.y + 8f, SIZE - 16f, SIZE - 16f);
    }

    @Override
    public void update(float delta, float targetX, float targetY) {
        animationTime += delta;
        hitTimer = Math.max(0f, hitTimer - delta);
        position.x -= 255f * delta;
        position.y = MathUtils.lerp(position.y, targetY - SIZE / 2f,
            Math.min(1f, delta * 1.45f));
        position.y += MathUtils.sin(animationTime * 8f) * 35f * delta;
        bounds.setPosition(position.x + 8f, position.y + 8f);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hitTimer > 0f) {
            batch.setColor(Color.RED);
        } else {
            batch.setColor(0.72f, 0.32f, 0.95f, 1f);
        }
        float pulse = 1f + MathUtils.sin(animationTime * 9f) * 0.08f;
        batch.draw(texture, position.x, position.y, SIZE * pulse, SIZE / pulse);
        batch.setColor(Color.WHITE);
    }

    @Override public boolean isOutOfScreen() { return position.x + SIZE < 0f; }
    @Override public Rectangle getBounds() { return bounds; }
    @Override public void hit() { hp--; hitTimer = 0.15f; }
    @Override public boolean isDead() { return hp <= 0; }
    @Override public void setSpeedMultiplier(float speed) { }
    @Override public int getScoreValue() { return 180; }
    @Override public float getCollisionDamage() { return 32f; }
}
