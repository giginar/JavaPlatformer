package com.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.game.settings.DisplaySettings;

public class FontManager {
    public static final String TURKISH_GLYPHS = "çÇğĞıİöÖşŞüÜ";
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
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + TURKISH_GLYPHS;
        parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        parameter.size = 18;
        smallFont = generator.generateFont(parameter);

        parameter.size = 28;
        mediumFont = generator.generateFont(parameter);

        parameter.size = 56;
        largeFont = generator.generateFont(parameter);

        generator.dispose();
        verifyTurkishGlyphs(smallFont);
        verifyTurkishGlyphs(mediumFont);
        verifyTurkishGlyphs(largeFont);
        setTextScale(DisplaySettings.textScale().multiplier());
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

    public static void setTextScale(float multiplier) {
        float scale = Math.max(1f, Math.min(1.15f, multiplier));
        if (smallFont != null) smallFont.getData().setScale(scale);
        if (mediumFont != null) mediumFont.getData().setScale(scale);
        // Large headings are already prominent and share tight vertical layouts.
        if (largeFont != null) largeFont.getData().setScale(1f);
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

    private static void verifyTurkishGlyphs(BitmapFont font) {
        for (char glyph : TURKISH_GLYPHS.toCharArray()) {
            if (!font.getData().hasGlyph(glyph)) {
                throw new IllegalStateException("Orbitron font is missing required Turkish glyph: " + glyph);
            }
        }
    }
}
