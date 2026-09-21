package com.game.i18n;

import com.game.manager.FontManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationResourcesTest {
    @Test
    void requiredTranslationKeysExistInBothLanguages() throws IOException {
        Properties english = load("messages.properties");
        Properties turkish = load("messages_tr.properties");
        assertEquals(english.stringPropertyNames(), turkish.stringPropertyNames());
        Set<String> representative = Set.of("main.play", "setup.title", "store.title",
            "achievements.title", "options.language", "controls.title", "about.title",
            "game.paused", "game.choose_upgrade", "stage.5.title",
            "powerup.pressure_shield.title", "achievement.total_lockdown.description");
        assertTrue(english.stringPropertyNames().containsAll(representative));
        assertFalse(english.values().stream().anyMatch(value -> value.toString().isBlank()));
        assertFalse(turkish.values().stream().anyMatch(value -> value.toString().isBlank()));
    }

    @Test
    void fallbackTextIsSafeBeforeBundlesAreAvailable() {
        assertEquals("English fallback", Localization.textOr("missing.key", "English fallback"));
    }

    @Test
    void generatedFontCharacterSetContainsEveryTurkishGlyph() {
        for (char glyph : "çÇğĞıİöÖşŞüÜ".toCharArray()) {
            assertTrue(FontManager.TURKISH_GLYPHS.indexOf(glyph) >= 0,
                () -> "Missing Turkish glyph configuration: " + glyph);
        }
    }

    private static Properties load(String name) throws IOException {
        Path file = projectRoot().resolve("assets/i18n").resolve(name);
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
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
