package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

public final class ThermalVent implements EnvironmentalHazard {
    private static final float WIDTH = 68f;
    private final Rectangle plume = new Rectangle();
    private final float speed;
    private float x = GameConfig.WORLD_WIDTH + 20f;
    private float cycle;

    public ThermalVent(float speed) {
        this.speed = speed;
    }

    @Override
    public void update(float delta) {
        x -= speed * delta;
        cycle = (cycle + delta) % 3.2f;
        float height = cycle >= 0.85f && cycle <= 2.15f
            ? 360f * MathUtils.clamp(Math.min((cycle - 0.85f) * 4f, (2.15f - cycle) * 4f), 0f, 1f)
            : 0f;
        plume.set(x, 0f, WIDTH, height);
    }

    @Override
    public void render(ShapeRenderer renderer) {
        renderer.setColor(0.35f, 0.15f, 0.08f, 1f);
        renderer.rect(x - 8f, 0f, WIDTH + 16f, 22f);
        if (plume.height > 0f) {
            renderer.setColor(1f, 0.32f, 0.08f, 0.42f);
            renderer.rect(plume.x, plume.y, plume.width, plume.height);
            renderer.setColor(1f, 0.72f, 0.18f, 0.5f);
            renderer.circle(x + WIDTH / 2f, plume.height, 22f, 12);
        } else {
            renderer.setColor(1f, 0.7f, 0.12f, 0.35f + MathUtils.sin(cycle * 16f) * 0.18f);
            renderer.rect(x, 22f, WIDTH, 7f);
        }
    }

    @Override
    public boolean collides(Rectangle target) {
        return plume.height > 0f && plume.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return x + WIDTH < 0f;
    }

    @Override
    public float collisionDamage() {
        return 30f;
    }

    @Override
    public boolean removeOnCollision() {
        return false;
    }
}
