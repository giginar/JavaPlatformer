package com.game.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeAreaViewportTest {
    @Test
    void representativeAspectRatiosRemainInsideTheAvailableDisplayArea() {
        int[][] sizes = {
            {1280, 720}, {2160, 1080}, {2340, 1080}, {2400, 1080},
            {1024, 768}, {3440, 1440}, {640, 360}
        };
        for (int[] size : sizes) {
            var bounds = SafeAreaViewport.calculateScreenBounds(
                1280f, 720f, size[0], size[1], 0, 0, 0, 0);
            assertTrue(bounds.x() >= 0 && bounds.y() >= 0);
            assertTrue(bounds.x() + bounds.width() <= size[0]);
            assertTrue(bounds.y() + bounds.height() <= size[1]);
            assertEquals(16f / 9f, (float) bounds.width() / bounds.height(), 0.01f);
        }
    }

    @Test
    void cutoutAndGestureInsetsAreExcludedBeforeLetterboxing() {
        var bounds = SafeAreaViewport.calculateScreenBounds(
            1280f, 720f, 2400, 1080, 120, 80, 24, 48);

        assertTrue(bounds.x() >= 120);
        assertTrue(bounds.y() >= 48);
        assertTrue(bounds.x() + bounds.width() <= 2400 - 80);
        assertTrue(bounds.y() + bounds.height() <= 1080 - 24);
        assertEquals(16f / 9f, (float) bounds.width() / bounds.height(), 0.01f);
    }

    @Test
    void invalidInsetsAreClampedWithoutProducingAnEmptyViewport() {
        var bounds = SafeAreaViewport.calculateScreenBounds(
            1280f, 720f, 640, 360, -10, 900, -5, 800);

        assertTrue(bounds.width() >= 1);
        assertTrue(bounds.height() >= 1);
        assertTrue(bounds.x() >= 0);
        assertTrue(bounds.y() >= 0);
    }
}
