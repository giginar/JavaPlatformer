package com.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {
    private static BitmapFont smallFont;
    private static BitmapFont mediumFont;
    private static BitmapFont largeFont;

    public static void initialize() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.color = Color.WHITE;
        parameter.size = 10;
        smallFont = generator.generateFont(parameter);

        parameter.size = 14;
        mediumFont = generator.generateFont(parameter);

        parameter.size = 18;
        largeFont = generator.generateFont(parameter);

        generator.dispose();
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
        smallFont.dispose();
        mediumFont.dispose();
        largeFont.dispose();
    }
}
