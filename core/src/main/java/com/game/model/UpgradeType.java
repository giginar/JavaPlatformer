package com.game.model;

public enum UpgradeType {
    RAPID_FIRE("RAPID FIRE", "Harpoon cooldown -18%", 3),
    PIERCING_HARPOON("PIERCING", "Harpoons pierce +1 target", 3),
    OXYGEN_EFFICIENCY("AIR RECYCLER", "Oxygen drain -15%", 3),
    LARGE_TANKS("PRESSURIZED TANKS", "Oxygen pickups restore +10", 3),
    AGILE_DIVER("HYDRO FINS", "Swimming agility +12%", 3);

    private final String title;
    private final String description;
    private final int maxLevel;

    UpgradeType(String title, String description, int maxLevel) {
        this.title = title;
        this.description = description;
        this.maxLevel = maxLevel;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public int maxLevel() {
        return maxLevel;
    }
}
