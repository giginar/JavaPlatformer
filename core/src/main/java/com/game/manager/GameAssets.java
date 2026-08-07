package com.game.manager;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public final class GameAssets {
    public static final String BACKGROUND = "background.png";
    public static final String DIVER = "diver.png";
    public static final String HARPOON = "harpoon.png";
    public static final String OXYGEN_TANK = "oxygen_tank.png";
    public static final String SMALL_FISH = "enemy_small.png";
    public static final String FAST_FISH = "enemy_fast.png";
    public static final String PIRANHA = "enemy_piranha.png";
    public static final String SHARK = "enemy_shark.png";

    private static final String[] TEXTURES = {
        BACKGROUND,
        DIVER,
        HARPOON,
        OXYGEN_TANK,
        SMALL_FISH,
        FAST_FISH,
        PIRANHA,
        SHARK
    };

    private static AssetManager manager;

    private GameAssets() {
    }

    public static void initialize() {
        if (manager != null) {
            return;
        }

        AssetManager newManager = new AssetManager();
        try {
            for (String path : TEXTURES) {
                newManager.load(path, Texture.class);
            }
            newManager.finishLoading();

            for (String path : TEXTURES) {
                Texture texture = newManager.get(path, Texture.class);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
            manager = newManager;
        } catch (RuntimeException exception) {
            newManager.dispose();
            throw exception;
        }
    }

    public static Texture texture(String path) {
        if (manager == null) {
            throw new IllegalStateException("GameAssets must be initialized before textures are requested.");
        }
        return manager.get(path, Texture.class);
    }

    public static void dispose() {
        if (manager == null) {
            return;
        }
        manager.dispose();
        manager = null;
    }
}
