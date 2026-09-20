package com.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GameConfigTest {
    @AfterEach
    void clearDevelopmentProperties() {
        System.clearProperty("deepdive.debug");
        System.clearProperty("deepdive.stageDurationSeconds");
    }

    @Test
    void runtimePropertiesCannotEnableProductionProgressionShortcuts() {
        System.setProperty("deepdive.debug", "true");
        System.setProperty("deepdive.stageDurationSeconds", "5");

        assertFalse(GameConfig.developmentShortcutsEnabled());
        assertEquals(GameConfig.DEFAULT_STAGE_DURATION_SECONDS,
            GameConfig.stageDurationSeconds());
    }
}
