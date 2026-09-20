package com.game.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameScreenTimingTest {
    private static final float MAX_FRAME_DELTA = 1f / 15f;

    @Test
    void normalFrameRatesKeepTheirElapsedGameTime() {
        for (int framesPerSecond : new int[]{30, 60, 90, 120}) {
            float delta = 1f / framesPerSecond;
            assertEquals(delta, GameScreen.safeFrameDelta(delta), 0f,
                framesPerSecond + " FPS");
        }
    }

    @Test
    void stallsAndResumeGapsCannotCreateSimulationCatchUp() {
        for (float delta : new float[]{0.1f, 0.25f, 0.5f, 1f, 8f}) {
            assertEquals(MAX_FRAME_DELTA, GameScreen.safeFrameDelta(delta), 0.000001f,
                delta + " second stall");
        }
    }

    @Test
    void invalidDeltasCannotCorruptSimulationTimers() {
        assertEquals(0f, GameScreen.safeFrameDelta(Float.NaN), 0f);
        assertEquals(0f, GameScreen.safeFrameDelta(Float.POSITIVE_INFINITY), 0f);
        assertEquals(0f, GameScreen.safeFrameDelta(-1f), 0f);
    }
}
