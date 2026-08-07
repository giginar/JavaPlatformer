package com.game.diver;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.game.GameConfig;
import com.game.manager.GameAssets;

public class Background {

    private final Texture texture;
    private float x1, x2;
    private final float scrollSpeed;
    private final float drawHeight;
    private final float drawY;

    public Background() {
        this(45f);
    }

    public Background(float scrollSpeed) {
        texture = GameAssets.texture(GameAssets.BACKGROUND);
        this.scrollSpeed = scrollSpeed;
        drawHeight = GameConfig.WORLD_WIDTH * texture.getHeight() / texture.getWidth();
        drawY = (GameConfig.WORLD_HEIGHT - drawHeight) / 2f;
        x1 = 0;
        x2 = GameConfig.WORLD_WIDTH;
    }

    public void update(float delta) {
        x1 -= scrollSpeed * delta;
        x2 -= scrollSpeed * delta;

        if (x1 + GameConfig.WORLD_WIDTH <= 0) {
            x1 = x2 + GameConfig.WORLD_WIDTH;
        }
        if (x2 + GameConfig.WORLD_WIDTH <= 0) {
            x2 = x1 + GameConfig.WORLD_WIDTH;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x1, drawY, GameConfig.WORLD_WIDTH, drawHeight);
        batch.draw(texture, x2, drawY, GameConfig.WORLD_WIDTH, drawHeight);
    }
}
