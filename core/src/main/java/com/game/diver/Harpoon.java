package com.game.diver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Harpoon {

    private Texture texture;
    private Vector2 position;
    private float speed = 400f;
    private float width = 32f;
    private float height = 8f;

    public Harpoon(float x, float y) {
        texture = new Texture("harpoon.png");
        position = new Vector2(x, y);
    }

    public void update(float delta) {
        position.x += speed * delta;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, width, height);
    }

    public boolean isOutOfScreen() {
        return position.x > Gdx.graphics.getWidth();
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, width, height);
    }

    public void dispose() {
        texture.dispose();
    }
}
