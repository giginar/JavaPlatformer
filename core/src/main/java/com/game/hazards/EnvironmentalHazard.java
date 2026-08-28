package com.game.hazards;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public interface EnvironmentalHazard {
    void update(float delta);
    void render(ShapeRenderer renderer);
    boolean collides(Rectangle target);
    boolean isOutOfScreen();
    float collisionDamage();

    default boolean removeOnCollision() {
        return true;
    }
}
