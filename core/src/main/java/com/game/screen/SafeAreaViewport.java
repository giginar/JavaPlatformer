package com.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Keeps the fixed logical canvas inside platform safe insets and letterboxes the remainder. */
public final class SafeAreaViewport extends Viewport {
    public SafeAreaViewport(float worldWidth, float worldHeight, Camera camera) {
        setWorldSize(worldWidth, worldHeight);
        setCamera(camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        ScreenBounds bounds = calculateScreenBounds(getWorldWidth(), getWorldHeight(),
            screenWidth, screenHeight,
            safeInset(Gdx.graphics.getSafeInsetLeft(), screenWidth),
            safeInset(Gdx.graphics.getSafeInsetRight(), screenWidth),
            safeInset(Gdx.graphics.getSafeInsetTop(), screenHeight),
            safeInset(Gdx.graphics.getSafeInsetBottom(), screenHeight));
        setScreenBounds(bounds.x(), bounds.y(), bounds.width(), bounds.height());
        apply(centerCamera);
    }

    public static ScreenBounds calculateScreenBounds(float worldWidth, float worldHeight,
                                                       int screenWidth, int screenHeight,
                                                       int safeLeft, int safeRight,
                                                       int safeTop, int safeBottom) {
        int left = clampInset(safeLeft, screenWidth);
        int right = clampInset(safeRight, Math.max(0, screenWidth - left));
        int bottom = clampInset(safeBottom, screenHeight);
        int top = clampInset(safeTop, Math.max(0, screenHeight - bottom));
        int usableWidth = Math.max(1, screenWidth - left - right);
        int usableHeight = Math.max(1, screenHeight - top - bottom);
        float scale = Math.min(usableWidth / worldWidth, usableHeight / worldHeight);
        int viewportWidth = Math.max(1, Math.round(worldWidth * scale));
        int viewportHeight = Math.max(1, Math.round(worldHeight * scale));
        int x = left + (usableWidth - viewportWidth) / 2;
        int y = bottom + (usableHeight - viewportHeight) / 2;
        return new ScreenBounds(x, y, viewportWidth, viewportHeight);
    }

    private static int safeInset(int value, int dimension) {
        return clampInset(value, dimension);
    }

    private static int clampInset(int value, int maximum) {
        return Math.max(0, Math.min(value, maximum));
    }

    public record ScreenBounds(int x, int y, int width, int height) {
    }
}
