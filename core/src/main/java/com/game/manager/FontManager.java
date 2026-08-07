package com.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {
    private static BitmapFont smallFont;
    private static BitmapFont mediumFont;
    private static BitmapFont largeFont;
    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.color = Color.WHITE;
        parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        parameter.size = 18;
        smallFont = generator.generateFont(parameter);

        parameter.size = 28;
        mediumFont = generator.generateFont(parameter);

        parameter.size = 56;
        largeFont = generator.generateFont(parameter);

        generator.dispose();
        initialized = true;
    }

    public static BitmapFont getSmallFont() {
        return smallFont;
    }

    public static BitmapFont getMediumFont() {
        return mediumFont;
    }

    public static BitmapFont getLargeFont() {
        return largeFont;
    }

    public static void dispose() {
        if (!initialized) {
            return;
        }
        smallFont.dispose();
        mediumFont.dispose();
        largeFont.dispose();
        smallFont = null;
        mediumFont = null;
        largeFont = null;
        initialized = false;
    }
}
