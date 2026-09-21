package com.game.model;

import com.game.i18n.Localization;
import java.util.Locale;

public enum PermanentUpgrade {
    PRESSURE_TANK("PRESSURE TANK", "+10 starting oxygen capacity per level", 8),
    REINFORCED_SUIT("REINFORCED SUIT", "+1.5 sec starting shield per level", 10),
    MAGNETIC_CLASP("MAGNETIC CLASP", "+35 pickup magnet range per level", 7),
    TWIN_LAUNCHER("TWIN LAUNCHER", "+1 starting harpoon pierce per level", 14),
    SALVAGE_MAP("SALVAGE MAP", "+15% pearl rewards per level", 9);

    private static final int MAX_LEVEL = 3;

    private final String title;
    private final String description;
    private final int baseCost;

    PermanentUpgrade(String title, String description, int baseCost) {
        this.title = title;
        this.description = description;
        this.baseCost = baseCost;
    }

    public String title() {
        return Localization.textOr(key("title"), title);
    }

    public String description() {
        return Localization.textOr(key("description"), description);
    }

    public int costForLevel(int currentLevel) {
        return baseCost * (currentLevel + 1);
    }

    public int maxLevel() {
        return MAX_LEVEL;
    }

    public long installDurationMillis(int targetLevel) {
        if (targetLevel < 1 || targetLevel > maxLevel()) {
            throw new IllegalArgumentException("Invalid equipment level: " + targetLevel);
        }
        return targetLevel * 5L * 60_000L;
    }

    private String key(String part) {
        return "permanent." + name().toLowerCase(Locale.ROOT) + "." + part;
    }
}
