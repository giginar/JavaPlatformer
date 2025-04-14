package com.game.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public interface EnemyFish {
    void update(float delta);
    void render(SpriteBatch batch);
    boolean isOutOfScreen();
    Rectangle getBounds();
    void dispose();
}
