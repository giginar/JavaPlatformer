package com.game.model;

public enum UpgradeType {
    RAPID_FIRE("RAPID FIRE", "Harpoon cooldown -18%"),
    PIERCING_HARPOON("PIERCING", "Harpoons pierce +1 target"),
    OXYGEN_EFFICIENCY("AIR RECYCLER", "Oxygen drain -15%"),
    LARGE_TANKS("PRESSURIZED TANKS", "Oxygen pickups restore +10"),
    AGILE_DIVER("HYDRO FINS", "Swimming agility +12%");

    private final String title;
    private final String description;

    UpgradeType(String title, String description) {
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
