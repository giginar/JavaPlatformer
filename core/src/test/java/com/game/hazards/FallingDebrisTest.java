package com.game.hazards;

import com.badlogic.gdx.math.MathUtils;
import com.game.GameConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallingDebrisTest {
    @Test
    void swaysToBothSidesWhileFalling() {
        FallingDebris debris = new FallingDebris(100f, 0f, 40f, MathUtils.PI, 1f);

        debris.update(0.9f);
        assertEquals(100f, debris.x(), 0.0001f);
        assertEquals(GameConfig.WORLD_HEIGHT + 8f, debris.y(), 0.0001f);

        debris.update(0.5f);
        assertEquals(140f, debris.x(), 0.0001f);

        debris.update(0.5f);
        assertEquals(100f, debris.x(), 0.0001f);

        debris.update(0.5f);
        assertEquals(60f, debris.x(), 0.0001f);
        assertTrue(debris.y() < GameConfig.WORLD_HEIGHT + 8f);
    }

    @Test
    void scrollingAlsoMovesTheCenterOfTheSway() {
        FallingDebris debris = new FallingDebris(200f, 20f, 40f, MathUtils.PI, -1f);

        debris.update(0.9f);
        debris.update(0.5f);

        assertEquals(132f, debris.x(), 0.0001f);
    }

    @Test
    void fallsSlowlyEnoughToRemainDodgeable() {
        FallingDebris debris = new FallingDebris(100f, 0f, 0f, 0f, 1f);
        float startingY = debris.y();

        debris.update(0.9f);
        for (int i = 0; i < 40; i++) {
            debris.update(0.1f);
        }

        float fallenDistance = startingY - debris.y();
        assertTrue(fallenDistance > 200f);
        assertTrue(fallenDistance < GameConfig.WORLD_HEIGHT / 2f);
    }
}
