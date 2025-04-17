package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class OxygenTank {

    private Texture texture;
    private float x, baseY;
    private final float speed = 120f;
    private final int width = 32;
    private final int height = 32;
    private Rectangle bounds;
    private float time;

    public OxygenTank(float x, float y) {
        this.texture = new Texture("oxygen_tank.png");
        this.x = x;
        this.baseY = y;
        this.bounds = new Rectangle(x, y, width, height);
        this.time = 0f;
    }

    public void update(float delta) {
        time += delta;
        x -= speed * delta;

        float wobble = MathUtils.sin(time * 3f) * 3f;
        float alpha = 0.85f + 0.15f * MathUtils.sin(time * 4f);

        bounds.setPosition(x, baseY + wobble);
    }

    public void render(SpriteBatch batch) {
        float wobble = MathUtils.sin(time * 3f) * 3f;
        batch.setColor(1f, 1f, 1f, 0.85f + 0.15f * MathUtils.sin(time * 4f));
        batch.draw(texture, x, baseY + wobble, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isOutOfScreen() {
        return x + width < 0;
    }

    public void dispose() {
        texture.dispose();
    }
}
