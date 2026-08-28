package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

public final class FallingRock implements EnvironmentalHazard {
    private static final float SIZE = 54f;
    private final Rectangle bounds;
    private final float scrollSpeed;
    private float warningTimer = 0.9f;
    private float fallVelocity;

    public FallingRock(float x, float scrollSpeed) {
        bounds = new Rectangle(x, GameConfig.WORLD_HEIGHT + 8f, SIZE, SIZE);
        this.scrollSpeed = scrollSpeed;
    }

    @Override
    public void update(float delta) {
        bounds.x -= scrollSpeed * delta;
        if (warningTimer > 0f) {
            warningTimer = Math.max(0f, warningTimer - delta);
            return;
        }
        fallVelocity = Math.min(560f, fallVelocity + 720f * delta);
        bounds.y -= fallVelocity * delta;
    }

    @Override
    public void render(ShapeRenderer renderer) {
        if (warningTimer > 0f) {
            float alpha = 0.25f + MathUtils.sin(warningTimer * 24f) * 0.15f;
            renderer.setColor(1f, 0.55f, 0.12f, alpha);
            renderer.rect(bounds.x + SIZE / 2f - 3f, 0f, 6f, GameConfig.WORLD_HEIGHT);
            return;
        }
        renderer.setColor(0.3f, 0.26f, 0.29f, 1f);
        renderer.circle(bounds.x + SIZE / 2f, bounds.y + SIZE / 2f, SIZE / 2f, 12);
        renderer.setColor(0.55f, 0.42f, 0.35f, 0.9f);
        renderer.circle(bounds.x + 19f, bounds.y + 36f, 8f, 8);
    }

    @Override
    public boolean collides(Rectangle target) {
        return warningTimer <= 0f && bounds.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return bounds.y + SIZE < 0f || bounds.x + SIZE < 0f;
    }

    @Override
    public float collisionDamage() {
        return 34f;
    }
}
