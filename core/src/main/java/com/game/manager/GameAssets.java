package com.game.manager;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class GameAssets {
    public static final String BACKGROUND = "background.png";
    public static final String DIVER = "diver.png";
    public static final String DIVER_GREEN = "diver_green.png";
    public static final String DIVER_RED = "diver_red.png";
    public static final String HARPOON = "harpoon.png";
    public static final String OXYGEN_TANK = "oxygen_tank.png";
    public static final String SMALL_FISH = "enemy_small.png";
    public static final String FAST_FISH = "enemy_fast.png";
    public static final String PIRANHA = "enemy_piranha.png";
    public static final String SHARK = "enemy_shark.png";
    public static final String OCTOPUS_BOSS = "enemy_octopus_boss.png";

    private static final String[] TEXTURES = {
        BACKGROUND,
        DIVER,
        DIVER_GREEN,
        DIVER_RED,
        HARPOON,
        OXYGEN_TANK,
        SMALL_FISH,
        FAST_FISH,
        PIRANHA,
        SHARK,
        OCTOPUS_BOSS
    };

    private static AssetManager manager;
    private static ShaderProgram diverOutlineShader;

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
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            ShaderProgram outlineShader = new ShaderProgram(
                Gdx.files.internal("shaders/diver-outline.vert"),
                Gdx.files.internal("shaders/diver-outline.frag"));
            if (outlineShader.isCompiled()) {
                diverOutlineShader = outlineShader;
            } else {
                // This cosmetic effect must not prevent play on a device with a shader issue.
                Gdx.app.error("GameAssets", "Diver outline unavailable: " + outlineShader.getLog());
                outlineShader.dispose();
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

    public static ShaderProgram diverOutlineShader() {
        return diverOutlineShader;
    }

    public static void dispose() {
        if (manager == null) {
            return;
        }
        manager.dispose();
        manager = null;
        if (diverOutlineShader != null) {
            diverOutlineShader.dispose();
            diverOutlineShader = null;
        }
    }
}
