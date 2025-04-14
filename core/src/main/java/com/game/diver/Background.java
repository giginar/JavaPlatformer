package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {

    private Texture texture;
    private float x1, x2;
    private float scrollSpeed = 100f;

    public Background() {
        texture = new Texture("background.png");
        x1 = 0;
        x2 = texture.getWidth();
    }

    public void update(float delta) {
        x1 -= scrollSpeed * delta;
        x2 -= scrollSpeed * delta;

        if (x1 + texture.getWidth() <= 0) {
            x1 = x2 + texture.getWidth();
        }
        if (x2 + texture.getWidth() <= 0) {
            x2 = x1 + texture.getWidth();
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x1, 0);
        batch.draw(texture, x2, 0);
    }

    public void dispose() {
        texture.dispose();
    }
}
