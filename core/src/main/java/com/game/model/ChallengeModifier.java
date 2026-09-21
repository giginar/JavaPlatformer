package com.game.model;

import com.game.i18n.Localization;
import java.util.Locale;

public enum ChallengeModifier {
    NO_WEAPON("NO HARPOON", "Harpoon firing is completely disabled"),
    NO_OXYGEN_PICKUPS("NO OXYGEN PICKUPS",
        "No oxygen tanks; sealed-loop drain is greatly reduced"),
    NO_POWER_UPS("NO SPECIAL POWERS", "Power-up pickups will not spawn"),
    NO_UPGRADES("NO UPGRADES", "Suit, gear and run upgrades are disabled");

    private final String title;
    private final String description;

    ChallengeModifier(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return Localization.textOr(key("title"), title);
    }

    public String description() {
        return Localization.textOr(key("description"), description);
    }

    private String key(String part) {
        return "challenge." + name().toLowerCase(Locale.ROOT) + "." + part;
    }
}
