package com.game.diver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Diver {

    private final Vector2 position;
    private final Vector2 velocity;
    private final float GRAVITY = -300f;
    private final float JUMP_VELOCITY = 200f;
    public static final float WIDTH = 64f;
    public static final float HEIGHT = 64f;

    private Texture texture;

    public Diver() {
        position = new Vector2(100, Gdx.graphics.getHeight() / 2f);
        velocity = new Vector2(0, 0);
        texture = new Texture("diver.png");
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            velocity.y = JUMP_VELOCITY;
        } else {
            velocity.y += GRAVITY * delta;
        }

        position.y += velocity.y * delta;

        if (position.y < 0) {
            position.y = 0;
            velocity.y = 0;
        }

        if (position.y > Gdx.graphics.getHeight() - HEIGHT) {
            position.y = Gdx.graphics.getHeight() - HEIGHT;
            velocity.y = 0;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, WIDTH, HEIGHT);
    }

    public void dispose() {
        texture.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }


}
