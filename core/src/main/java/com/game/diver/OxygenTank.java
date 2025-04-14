package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class OxygenTank {

    private Texture texture;
    private float x, y;
    private final float speed = 120f;
    private final int width = 32;
    private final int height = 32;
    private Rectangle bounds;

    public OxygenTank(float x, float y) {
        this.texture = new Texture("oxygen_tank.png"); // place this in assets
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void update(float delta) {
        x -= speed * delta;
        bounds.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
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
