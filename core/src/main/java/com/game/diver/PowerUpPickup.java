package com.game.diver;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.model.PowerUpType;

public final class PowerUpPickup {
    private static final float SIZE = 38f;
    private final PowerUpType type;
    private final Rectangle bounds;
    private final float baseY;
    private float time;

    public PowerUpPickup(PowerUpType type, float x, float y) {
        this.type = type;
        baseY = y;
        bounds = new Rectangle(x, y, SIZE, SIZE);
    }

    public void update(float delta, float targetX, float targetY, float magnetRange) {
        time += delta;
        bounds.x -= 120f * delta;
        bounds.y = baseY + MathUtils.sin(time * 3.5f) * 10f;
        float dx = targetX - bounds.x;
        float dy = targetY - bounds.y;
        if (dx * dx + dy * dy <= magnetRange * magnetRange) {
            bounds.x = MathUtils.lerp(bounds.x, targetX, Math.min(1f, delta * 5.5f));
            bounds.y = MathUtils.lerp(bounds.y, targetY, Math.min(1f, delta * 5.5f));
        }
    }

    public void render(ShapeRenderer renderer) {
        Color color = switch (type) {
            case PRESSURE_SHIELD -> Color.CYAN;
            case TIME_BUBBLE -> Color.VIOLET;
            case MAGNETIC_CURRENT -> Color.GOLD;
            case HARPOON_OVERDRIVE -> Color.ORANGE;
            case TORPEDO_DASH -> Color.LIME;
        };
        float cx = bounds.x + SIZE / 2f;
        float cy = bounds.y + SIZE / 2f;
        renderer.setColor(color.r, color.g, color.b, 0.25f);
        renderer.circle(cx, cy, 25f + MathUtils.sin(time * 5f) * 3f, 18);
        renderer.setColor(color);
        renderer.circle(cx, cy, 13f, 14);
        renderer.setColor(Color.WHITE);
        renderer.rect(cx - 3f, cy - 9f, 6f, 18f);
        renderer.rect(cx - 9f, cy - 3f, 18f, 6f);
    }

    public Rectangle bounds() {
        return bounds;
    }

    public PowerUpType type() {
        return type;
    }

    public boolean isOutOfScreen() {
        return bounds.x + SIZE < 0f;
    }
}
