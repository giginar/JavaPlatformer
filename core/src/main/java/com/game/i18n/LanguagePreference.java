package com.game.i18n;

import com.badlogic.gdx.Preferences;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Backward-compatible language preference state, independent from save migrations. */
public final class LanguagePreference {
    private final Preferences preferences;
    private final GameLanguage suggested;
    private GameLanguage current;
    private boolean selectionRequired;

    public LanguagePreference(Preferences preferences, Locale deviceLocale) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        suggested = GameLanguage.suggestedFor(deviceLocale);
        Optional<GameLanguage> stored = GameLanguage.fromCode(
            preferences.getString(Localization.LANGUAGE_KEY, ""));
        current = stored.orElse(GameLanguage.ENGLISH);
        selectionRequired = stored.isEmpty();
    }

    public GameLanguage current() {
        return current;
    }

    public GameLanguage suggested() {
        return suggested;
    }

    public boolean selectionRequired() {
        return selectionRequired;
    }

    public void select(GameLanguage language) {
        current = Objects.requireNonNull(language, "language");
        selectionRequired = false;
        preferences.putString(Localization.LANGUAGE_KEY, language.code());
        preferences.flush();
    }
}
