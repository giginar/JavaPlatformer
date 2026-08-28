package com.game.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.game.hazards.EnvironmentalHazard;

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

    /** Ranged enemies return a shot once when their authored telegraph completes. */
    default EnemyProjectile pollProjectile(float targetX, float targetY) {
        return null;
    }

    default EnemyFish pollSpawnedEnemy() {
        return null;
    }

    default EnvironmentalHazard pollHazard() {
        return null;
    }
}
