package com.game.model;

public enum PermanentUpgrade {
    PRESSURE_TANK("PRESSURE TANK", "+10 starting oxygen capacity", 8, 4),
    REINFORCED_SUIT("REINFORCED SUIT", "+1.5 sec starting shield", 10, 4),
    MAGNETIC_CLASP("MAGNETIC CLASP", "+35 pickup magnet range", 7, 4),
    TWIN_LAUNCHER("TWIN LAUNCHER", "+1 starting harpoon pierce", 14, 2),
    SALVAGE_MAP("SALVAGE MAP", "+15% pearl rewards", 9, 4);

    private final String title;
    private final String description;
    private final int baseCost;
    private final int maxLevel;

    PermanentUpgrade(String title, String description, int baseCost, int maxLevel) {
        this.title = title;
        this.description = description;
        this.baseCost = baseCost;
        this.maxLevel = maxLevel;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public int costForLevel(int currentLevel) {
        return baseCost * (currentLevel + 1);
    }

    public int maxLevel() {
        return maxLevel;
    }
}
