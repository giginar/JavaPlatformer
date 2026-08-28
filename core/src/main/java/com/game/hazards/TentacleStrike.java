package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

/** A boss tentacle sweeps a telegraphed horizontal lane from the right edge. */
public final class TentacleStrike implements EnvironmentalHazard {
    private static final float LANE_HEIGHT = 88f;
    private final Rectangle bounds;
    private float lifetime;

    public TentacleStrike(float targetY) {
        float y = MathUtils.clamp(targetY - LANE_HEIGHT / 2f,
            20f, GameConfig.WORLD_HEIGHT - LANE_HEIGHT - 20f);
        bounds = new Rectangle(0f, y, GameConfig.WORLD_WIDTH, LANE_HEIGHT);
    }

    @Override
    public void update(float delta) {
        lifetime += delta;
    }

    @Override
    public void render(ShapeRenderer renderer) {
        if (lifetime < 0.95f) {
            float pulse = 0.18f + MathUtils.sin(lifetime * 28f) * 0.12f;
            renderer.setColor(1f, 0.1f, 0.32f, pulse);
            renderer.rect(0f, bounds.y, GameConfig.WORLD_WIDTH, bounds.height);
            renderer.setColor(1f, 0.35f, 0.55f, 0.75f);
            renderer.rect(0f, bounds.y + bounds.height / 2f - 2f,
                GameConfig.WORLD_WIDTH, 4f);
        } else if (lifetime < 1.42f) {
            renderer.setColor(0.22f, 0.025f, 0.34f, 0.98f);
            renderer.rect(0f, bounds.y + 13f, GameConfig.WORLD_WIDTH, bounds.height - 26f);
            renderer.setColor(0.58f, 0.12f, 0.72f, 0.95f);
            for (int i = 0; i < 16; i++) {
                renderer.circle(50f + i * 84f, bounds.y + bounds.height / 2f,
                    12f + MathUtils.sin(i * 1.7f) * 3f, 10);
            }
        }
    }

    @Override
    public boolean collides(Rectangle target) {
        return lifetime >= 0.95f && lifetime < 1.42f && bounds.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return lifetime >= 1.55f;
    }

    @Override
    public float collisionDamage() {
        return 100f;
    }

    @Override
    public boolean removeOnCollision() {
        return false;
    }
}
