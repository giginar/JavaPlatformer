package com.game.model;

public enum UpgradeType {
    RAPID_FIRE("RAPID FIRE", "Harpoon cooldown -18%", 4),
    PIERCING_HARPOON("PIERCING", "Harpoons pierce +1 target", 0),
    OXYGEN_EFFICIENCY("AIR RECYCLER", "Oxygen drain -15%", 4),
    LARGE_TANKS("PRESSURIZED TANKS", "Oxygen pickups restore +10", 17),
    AGILE_DIVER("HYDRO FINS", "Swimming agility +12%", 0);

    private final String title;
    private final String description;
    private final int maximumUsefulLevel;

    UpgradeType(String title, String description, int maximumUsefulLevel) {
        this.title = title;
        this.description = description;
        this.maximumUsefulLevel = maximumUsefulLevel;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    /** Zero means the effect has no finite gameplay cap. */
    public int maximumUsefulLevel() {
        return maximumUsefulLevel;
    }

    public boolean isMaxed(int level) {
        return maximumUsefulLevel > 0 && level >= maximumUsefulLevel;
    }
}
