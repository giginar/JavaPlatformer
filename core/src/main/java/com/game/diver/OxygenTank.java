package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.game.manager.GameAssets;

public class OxygenTank {

    private final Texture texture;
    private float x, baseY;
    private static final float SPEED = 135f;
    private static final float WIDTH = 38f;
    private static final float HEIGHT = 48f;
    private final Rectangle bounds;
    private float time;

    public OxygenTank(float x, float y) {
        this.texture = GameAssets.texture(GameAssets.OXYGEN_TANK);
        this.x = x;
        this.baseY = y;
        this.bounds = new Rectangle(x, y, WIDTH, HEIGHT);
        this.time = 0f;
    }

    public void update(float delta) {
        update(delta, Float.NaN, Float.NaN, 0f);
    }

    public void update(float delta, float targetX, float targetY, float magnetRange) {
        time += delta;
        x -= SPEED * delta;

        float wobble = MathUtils.sin(time * 3f) * 3f;
        bounds.setPosition(x, baseY + wobble);
        float dx = targetX - bounds.x;
        float dy = targetY - bounds.y;
        if (magnetRange > 0f && dx * dx + dy * dy <= magnetRange * magnetRange) {
            x = MathUtils.lerp(x, targetX, Math.min(1f, delta * 5f));
            baseY = MathUtils.lerp(baseY, targetY, Math.min(1f, delta * 5f));
            bounds.setPosition(x, baseY + wobble);
        }
    }

    public void render(SpriteBatch batch) {
        float wobble = MathUtils.sin(time * 3f) * 3f;
        batch.setColor(1f, 1f, 1f, 0.85f + 0.15f * MathUtils.sin(time * 4f));
        batch.draw(texture, x, baseY + wobble, WIDTH, HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isOutOfScreen() {
        return x + WIDTH < 0;
    }
}
