package com.game.enemies;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.game.GameConfig;

public final class EnemyProjectile {
    private static final float ELECTRIC_SIZE = 18f;
    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private final Kind kind;
    private final float size;

    public EnemyProjectile(float x, float y, float targetX, float targetY, float speed) {
        this(Kind.ELECTRIC, x, y, targetX, targetY, speed, ELECTRIC_SIZE);
    }

    private EnemyProjectile(Kind kind, float x, float y, float targetX, float targetY,
                            float speed, float size) {
        this.kind = kind;
        this.size = size;
        position = new Vector2(x, y);
        velocity = new Vector2(targetX - x, targetY - y).nor().scl(speed);
        bounds = new Rectangle(x - size / 2f, y - size / 2f, size, size);
    }

    public static EnemyProjectile ink(float x, float y, float targetX, float targetY) {
        return new EnemyProjectile(Kind.INK, x, y, targetX, targetY, 235f, 34f);
    }

    public void update(float delta) {
        position.mulAdd(velocity, delta);
        bounds.setPosition(position.x - size / 2f, position.y - size / 2f);
    }

    public void render(ShapeRenderer renderer) {
        if (kind == Kind.INK) {
            renderer.setColor(0.08f, 0.01f, 0.13f, 0.85f);
            renderer.circle(position.x, position.y, size * 0.72f, 15);
            renderer.setColor(0.42f, 0.08f, 0.58f, 0.9f);
            renderer.circle(position.x - 6f, position.y + 7f, size * 0.3f, 11);
            renderer.circle(position.x + 9f, position.y - 5f, size * 0.24f, 10);
        } else {
            renderer.setColor(0.4f, 0.9f, 1f, 0.3f);
            renderer.circle(position.x, position.y, 15f, 14);
            renderer.setColor(0.7f, 1f, 1f, 1f);
            renderer.circle(position.x, position.y, 7f, 12);
        }
    }

    public Rectangle bounds() {
        return bounds;
    }

    public boolean isOutOfScreen() {
        return position.x < -size || position.x > GameConfig.WORLD_WIDTH + size
            || position.y < -size || position.y > GameConfig.WORLD_HEIGHT + size;
    }

    public float damage() {
        return kind == Kind.INK ? 0f : 22f;
    }

    public boolean shouldBurstIntoInk() {
        return kind == Kind.INK && position.x <= 360f;
    }

    public boolean isInk() {
        return kind == Kind.INK;
    }

    private enum Kind {
        ELECTRIC,
        INK
    }
}
