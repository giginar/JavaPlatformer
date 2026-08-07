package com.game.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public interface EnemyFish {
    void update(float delta, float targetX, float targetY);
    void render(SpriteBatch batch);
    boolean isOutOfScreen();
    Rectangle getBounds();
    void hit();
    boolean isDead();
    void setSpeedMultiplier(float speed);
    int getScoreValue();
    float getCollisionDamage();

    default boolean isBoss() {
        return false;
    }

    default boolean isTelegraphing() {
        return false;
    }

    default boolean removeOnPlayerCollision() {
        return true;
    }

    default float getHealthRatio() {
        return 0f;
    }
}
