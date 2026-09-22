package com.game.i18n;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;

/** Authoritative runtime localization and stable language preference. */
public final class Localization {
    public static final String LANGUAGE_KEY = "language.code";
    public static final String BUNDLE_PATH = "i18n/messages";

    private static LanguagePreference preference;
    private static I18NBundle english;
    private static I18NBundle selected;
    private static GameLanguage language = GameLanguage.ENGLISH;
    private static GameLanguage suggested = GameLanguage.ENGLISH;
    private static boolean selectionRequired = true;

    private Localization() {
    }

    public static void initialize(Preferences preferenceStore, Locale deviceLocale) {
        preference = new LanguagePreference(Objects.requireNonNull(preferenceStore, "preferenceStore"), deviceLocale);
        suggested = preference.suggested();
        String override = System.getProperty("deepdive.language", "").trim();
        if (!override.isEmpty()) {
            language = GameLanguage.fromCode(override).orElse(GameLanguage.ENGLISH);
            selectionRequired = false;
        } else {
            language = preference.current();
            selectionRequired = preference.selectionRequired();
        }
        loadBundles();
    }

    private static void loadBundles() {
        FileHandle base = Gdx.files.internal(BUNDLE_PATH);
        english = I18NBundle.createBundle(base, Locale.ROOT);
        selected = language == GameLanguage.ENGLISH
            ? english : I18NBundle.createBundle(base, language.locale());
    }

    public static boolean selectionRequired() {
        return selectionRequired;
    }

    public static GameLanguage language() {
        return language;
    }

    public static GameLanguage suggestedLanguage() {
        return suggested;
    }

    public static void select(GameLanguage selectedLanguage) {
        language = Objects.requireNonNull(selectedLanguage, "selectedLanguage");
        selectionRequired = false;
        preference.select(language);
        loadBundles();
    }

    public static String text(String key, Object... arguments) {
        return textOr(key, "!" + key + "!", arguments);
    }

    public static String textOr(String key, String fallback, Object... arguments) {
        if (selected == null || english == null) {
            return arguments.length == 0 ? fallback : format(fallback, arguments);
        }
        String pattern;
        try {
            pattern = selected.get(key);
        } catch (MissingResourceException missingSelectedTranslation) {
            try {
                pattern = english.get(key);
            } catch (MissingResourceException missingEnglishTranslation) {
                pattern = fallback;
            }
        }
        return arguments.length == 0 ? pattern : format(pattern, arguments);
    }

    public static boolean hasEnglishKey(String key) {
        if (english == null) return false;
        try {
            english.get(key);
            return true;
        } catch (MissingResourceException missing) {
            return false;
        }
    }

    private static String format(String pattern, Object... arguments) {
        return new java.text.MessageFormat(pattern, language.locale()).format(arguments);
    }
}
