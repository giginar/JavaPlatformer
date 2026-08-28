package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.GameConfig;

/** Two moving ruin slabs create a narrowing passage instead of a simple speed check. */
public final class CrushingGate implements EnvironmentalHazard {
    private static final float WIDTH = 72f;
    private final Rectangle lower = new Rectangle();
    private final Rectangle upper = new Rectangle();
    private final float speed;
    private final float gapCenter;
    private float x = GameConfig.WORLD_WIDTH + 20f;
    private float time;

    public CrushingGate(float gapCenter, float speed) {
        this.gapCenter = MathUtils.clamp(gapCenter, 220f, 500f);
        this.speed = speed;
        updateBounds();
    }

    @Override
    public void update(float delta) {
        time += delta;
        x -= speed * delta;
        updateBounds();
    }

    private void updateBounds() {
        float gap = 205f - (0.5f + 0.5f * MathUtils.sin(time * 2.7f)) * 62f;
        float lowerHeight = Math.max(0f, gapCenter - gap / 2f);
        float upperY = Math.min(GameConfig.WORLD_HEIGHT, gapCenter + gap / 2f);
        lower.set(x, 0f, WIDTH, lowerHeight);
        upper.set(x, upperY, WIDTH, GameConfig.WORLD_HEIGHT - upperY);
    }

    @Override
    public void render(ShapeRenderer renderer) {
        renderer.setColor(0.18f, 0.16f, 0.29f, 0.98f);
        renderer.rect(lower.x, lower.y, lower.width, lower.height);
        renderer.rect(upper.x, upper.y, upper.width, upper.height);
        renderer.setColor(0.5f, 0.33f, 0.68f, 0.95f);
        renderer.rect(lower.x + 9f, Math.max(0f, lower.height - 12f), lower.width - 18f, 8f);
        renderer.rect(upper.x + 9f, upper.y + 4f, upper.width - 18f, 8f);
    }

    @Override
    public boolean collides(Rectangle target) {
        return lower.overlaps(target) || upper.overlaps(target);
    }

    @Override
    public boolean isOutOfScreen() {
        return x + WIDTH < 0f;
    }

    @Override
    public float collisionDamage() {
        return 38f;
    }

    @Override
    public boolean removeOnCollision() {
        return false;
    }
}
