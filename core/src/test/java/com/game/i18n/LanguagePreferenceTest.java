package com.game.i18n;

import com.game.model.MemoryPreferences;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguagePreferenceTest {
    @Test
    void noStoredLanguageRequiresExplicitChoiceAndOnlySuggestsDeviceLocale() {
        LanguagePreference preference = new LanguagePreference(new MemoryPreferences(),
            Locale.forLanguageTag("tr-TR"));
        assertTrue(preference.selectionRequired());
        assertEquals(GameLanguage.ENGLISH, preference.current());
        assertEquals(GameLanguage.TURKISH, preference.suggested());
    }

    @Test
    void englishSelectionPersistsAndSubsequentStartupSkipsChooser() {
        MemoryPreferences backend = new MemoryPreferences();
        new LanguagePreference(backend, Locale.ENGLISH).select(GameLanguage.ENGLISH);
        LanguagePreference restarted = new LanguagePreference(backend, Locale.forLanguageTag("tr"));
        assertEquals("en", backend.getString(Localization.LANGUAGE_KEY));
        assertFalse(restarted.selectionRequired());
        assertEquals(GameLanguage.ENGLISH, restarted.current());
    }

    @Test
    void turkishSelectionPersistsAndSubsequentStartupSkipsChooser() {
        MemoryPreferences backend = new MemoryPreferences();
        new LanguagePreference(backend, Locale.ENGLISH).select(GameLanguage.TURKISH);
        LanguagePreference restarted = new LanguagePreference(backend, Locale.ENGLISH);
        assertEquals("tr", backend.getString(Localization.LANGUAGE_KEY));
        assertFalse(restarted.selectionRequired());
        assertEquals(GameLanguage.TURKISH, restarted.current());
    }

    @Test
    void languageCanBeChangedLater() {
        MemoryPreferences backend = new MemoryPreferences();
        LanguagePreference preference = new LanguagePreference(backend, Locale.ENGLISH);
        preference.select(GameLanguage.ENGLISH);
        preference.select(GameLanguage.TURKISH);
        assertEquals(GameLanguage.TURKISH, preference.current());
        assertEquals("tr", backend.getString(Localization.LANGUAGE_KEY));
    }

    @Test
    void invalidStoredLanguageFallsBackSafelyAndRequiresAValidChoice() {
        MemoryPreferences backend = new MemoryPreferences();
        backend.putString(Localization.LANGUAGE_KEY, "xx");
        LanguagePreference preference = new LanguagePreference(backend, Locale.ENGLISH);
        assertEquals(GameLanguage.ENGLISH, preference.current());
        assertTrue(preference.selectionRequired());
    }

    @Test
    void legacyPreferencesRemainUntouched() {
        MemoryPreferences backend = new MemoryPreferences();
        backend.putInteger("progression.pearls", 42).putString("progression.suit.selected", "RESCUE_RED");
        new LanguagePreference(backend, Locale.ENGLISH).select(GameLanguage.TURKISH);
        assertEquals(42, backend.getInteger("progression.pearls"));
        assertEquals("RESCUE_RED", backend.getString("progression.suit.selected"));
    }
}
