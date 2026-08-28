package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

/** A low or high reef shelf that teaches lane changes before faster threats appear. */
public final class ReefBarrier implements EnvironmentalHazard {
    private static final float WIDTH = 82f;
    private final Rectangle bounds;
    private final boolean ceiling;
    private final float speed;

    public ReefBarrier(boolean ceiling, float height, float speed) {
        this.ceiling = ceiling;
        this.speed = speed;
        float clampedHeight = Math.max(95f, Math.min(245f, height));
        float y = ceiling ? GameConfig.WORLD_HEIGHT - clampedHeight : 0f;
        bounds = new Rectangle(GameConfig.WORLD_WIDTH + 25f, y, WIDTH, clampedHeight);
    }

    @Override
    public void update(float delta) {
        bounds.x -= speed * delta;
    }

    @Override
    public void render(ShapeRenderer renderer) {
        renderer.setColor(0.08f, 0.34f, 0.31f, 0.96f);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        renderer.setColor(0.18f, 0.58f, 0.46f, 0.92f);
        float edgeY = ceiling ? bounds.y : bounds.y + bounds.height;
        float spike = ceiling ? -24f : 24f;
        for (int i = 0; i < 4; i++) {
            float x = bounds.x + i * 21f;
            renderer.triangle(x, edgeY, x + 10f, edgeY + spike, x + 21f, edgeY);
        }
    }

    @Override
    public boolean collides(Rectangle target) {
        return bounds.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return bounds.x + bounds.width < 0f;
    }

    @Override
    public float collisionDamage() {
        return 24f;
    }
}
