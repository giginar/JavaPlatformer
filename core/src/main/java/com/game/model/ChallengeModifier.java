package com.game.model;

public enum ChallengeModifier {
    NO_WEAPON("NO HARPOON", "Harpoon firing is completely disabled"),
    NO_OXYGEN_PICKUPS("NO OXYGEN PICKUPS", "Oxygen tanks will not spawn"),
    NO_POWER_UPS("NO SPECIAL POWERS", "Power-up pickups will not spawn"),
    NO_UPGRADES("NO UPGRADES", "Suit, gear and run upgrades are disabled");

    private final String title;
    private final String description;

    ChallengeModifier(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}
