package com.game.i18n;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum GameLanguage {
    ENGLISH("en", Locale.ENGLISH, "English"),
    TURKISH("tr", Locale.forLanguageTag("tr"), "Türkçe");

    private final String code;
    private final Locale locale;
    private final String nativeName;

    GameLanguage(String code, Locale locale, String nativeName) {
        this.code = code;
        this.locale = locale;
        this.nativeName = nativeName;
    }

    public String code() {
        return code;
    }

    public Locale locale() {
        return locale;
    }

    public String nativeName() {
        return nativeName;
    }

    public static Optional<GameLanguage> fromCode(String code) {
        if (code == null) return Optional.empty();
        return Arrays.stream(values())
            .filter(language -> language.code.equalsIgnoreCase(code.trim()))
            .findFirst();
    }

    public static GameLanguage suggestedFor(Locale locale) {
        return locale != null && "tr".equalsIgnoreCase(locale.getLanguage())
            ? TURKISH : ENGLISH;
    }
}
