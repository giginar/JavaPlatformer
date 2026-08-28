package com.game.model;

public enum PowerUpType {
    PRESSURE_SHIELD("PRESSURE SHIELD", 7f, true,
        "AUTO-ACTIVE  |  BLOCKS DAMAGE"),
    TIME_BUBBLE("TIME BUBBLE", 5f, true,
        "AUTO-ACTIVE  |  SLOWS ENEMIES AND HAZARDS"),
    MAGNETIC_CURRENT("MAGNETIC CURRENT", 10f, true,
        "AUTO-ACTIVE  |  PULLS PICKUPS TOWARD YOU"),
    HARPOON_OVERDRIVE("HARPOON OVERDRIVE", 8f, true,
        "AUTO-ACTIVE  |  FASTER, PIERCING HARPOONS"),
    TORPEDO_DASH("TORPEDO DASH", 1.2f, false,
        "PICKUP STORES A DASH CHARGE");

    private final String title;
    private final float duration;
    private final boolean activatesOnPickup;
    private final String usageHint;

    PowerUpType(String title, float duration, boolean activatesOnPickup, String usageHint) {
        this.title = title;
        this.duration = duration;
        this.activatesOnPickup = activatesOnPickup;
        this.usageHint = usageHint;
    }

    public String title() {
        return title;
    }

    public float duration() {
        return duration;
    }

    public boolean activatesOnPickup() {
        return activatesOnPickup;
    }

    public String usageHint() {
        return usageHint;
    }
}
