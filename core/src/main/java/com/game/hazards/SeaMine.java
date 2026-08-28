package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

public final class SeaMine implements EnvironmentalHazard {
    private static final float SIZE = 48f;
    private final Rectangle bounds;
    private final float baseY;
    private final float speed;
    private float time;

    public SeaMine(float y, float speed) {
        baseY = y;
        this.speed = speed;
        bounds = new Rectangle(GameConfig.WORLD_WIDTH + 20f, y, SIZE, SIZE);
    }

    @Override
    public void update(float delta) {
        time += delta;
        bounds.x -= speed * delta;
        bounds.y = baseY + MathUtils.sin(time * 2.4f) * 34f;
    }

    @Override
    public void render(ShapeRenderer renderer) {
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y + SIZE / 2f;
        renderer.setColor(0.08f, 0.09f, 0.13f, 1f);
        renderer.circle(cx, cy, 22f, 14);
        renderer.setColor(0.95f, 0.22f, 0.12f,
            0.65f + MathUtils.sin(time * 9f) * 0.3f);
        renderer.circle(cx, cy, 7f, 10);
        renderer.setColor(0.5f, 0.55f, 0.61f, 1f);
        renderer.rect(cx - 2f, cy + 20f, 4f, 12f);
        renderer.rect(cx - 2f, cy - 32f, 4f, 12f);
        renderer.rect(cx + 20f, cy - 2f, 12f, 4f);
        renderer.rect(cx - 32f, cy - 2f, 12f, 4f);
    }

    @Override
    public boolean collides(Rectangle target) {
        return bounds.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return bounds.x + SIZE < 0f;
    }

    @Override
    public float collisionDamage() {
        return 45f;
    }
}
