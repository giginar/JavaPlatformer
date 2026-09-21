package com.game.manager;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FontManagerTest {
    @Test
    void actualRuntimeFontGenerationProducesEveryTurkishGlyphAtEveryUiSize() {
        GdxNativesLoader.load();
        Path fontDirectory = projectRoot().resolve("assets/fonts");
        FreeTypeFontGenerator generator = generator(fontDirectory.resolve("Oxanium-Regular.ttf"));
        try {
            for (int size : new int[] {18, 28, 56}) {
                assertGeneratedGlyphsAtSize(generator, size);
            }
        } finally {
            generator.dispose();
        }
    }

    private static void assertGeneratedGlyphsAtSize(FreeTypeFontGenerator generator, int size) {
        PixmapPacker packer = packer();
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = parameter(size);
            parameter.packer = packer;
            FreeTypeFontGenerator.FreeTypeBitmapFontData fontData = generator.generateData(parameter);

            for (char character : FontManager.TURKISH_GLYPHS.toCharArray()) {
                BitmapFont.Glyph renderedGlyph = fontData.getGlyph(character);
                assertTrue(FontManager.isRenderable(renderedGlyph),
                    () -> String.format("U+%04X did not render at %d px", (int) character, size));
            }
        } finally {
            packer.dispose();
        }
    }

    private static FreeTypeFontGenerator.FreeTypeFontParameter parameter(int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = Color.WHITE;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + FontManager.TURKISH_GLYPHS;
        return parameter;
    }

    private static PixmapPacker packer() {
        return new PixmapPacker(512, 512, Pixmap.Format.RGBA8888, 1, false);
    }

    private static FreeTypeFontGenerator generator(Path path) {
        return new FreeTypeFontGenerator(new FileHandle(path.toFile()));
    }

    private static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve("assets"))
                && Files.isRegularFile(current.resolve("pom.xml"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("Project root not found");
    }
}
