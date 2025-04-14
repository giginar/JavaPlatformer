package com.diver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Diver {

    private final Vector2 position;
    private final Vector2 velocity;
    private final float GRAVITY = -300f;
    private final float JUMP_VELOCITY = 200f;
    private final float WIDTH = 40f;
    private final float HEIGHT = 20f;

    public Diver() {
        position = new Vector2(100, Gdx.graphics.getHeight() / 2f);
        velocity = new Vector2(0, 0);
    }

    public void update(float delta) {
        // Jetpack tarzı kontrol
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            velocity.y = JUMP_VELOCITY;
        } else {
            velocity.y += GRAVITY * delta;
        }

        position.y += velocity.y * delta;

        // Alt ve üst sınırlarda kal
        if (position.y < 0) {
            position.y = 0;
            velocity.y = 0;
        }

        if (position.y > Gdx.graphics.getHeight() - HEIGHT) {
            position.y = Gdx.graphics.getHeight() - HEIGHT;
            velocity.y = 0;
        }
    }

    public void render(ShapeRenderer renderer) {
        renderer.rect(position.x, position.y, WIDTH, HEIGHT);
    }
}
