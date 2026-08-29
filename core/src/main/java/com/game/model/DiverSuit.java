package com.game.model;

import com.game.GameConfig;

/** Unlockable diver suits. Each suit trades its color identity for one specialization. */
public enum DiverSuit {
    TIDELINE_BLUE(
        "TIDELINE BLUE",
        "Harpoon reloads 10% faster",
        0,
        "diver.png",
        0f,
        1f,
        0f,
        GameConfig.TIDELINE_BLUE_RELOAD_MULTIPLIER
    ),
    SALVAGE_GREEN(
        "SALVAGE GREEN",
        "+90 pickup magnet range",
        GameConfig.SALVAGE_GREEN_SUIT_COST,
        "diver_green.png",
        0f,
        1f,
        GameConfig.SALVAGE_GREEN_MAGNET_BONUS,
        1f
    ),
    RESCUE_RED(
        "RESCUE RED",
        "+25 starting oxygen capacity",
        GameConfig.RESCUE_RED_SUIT_COST,
        "diver_red.png",
        GameConfig.RESCUE_RED_OXYGEN_BONUS,
        1f,
        0f,
        1f
    ),
    ABYSS_BLACK(
        "ABYSS BLACK",
        "Swimming agility +15%",
        GameConfig.ABYSS_BLACK_SUIT_COST,
        "diver.png",
        0f,
        GameConfig.ABYSS_BLACK_AGILITY_MULTIPLIER,
        0f,
        1f
    );

    private final String title;
    private final String description;
    private final int cost;
    private final String texturePath;
    private final float oxygenBonus;
    private final float agilityMultiplier;
    private final float magnetBonusRange;
    private final float reloadMultiplier;

    DiverSuit(String title, String description, int cost, String texturePath,
              float oxygenBonus, float agilityMultiplier, float magnetBonusRange,
              float reloadMultiplier) {
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.texturePath = texturePath;
        this.oxygenBonus = oxygenBonus;
        this.agilityMultiplier = agilityMultiplier;
        this.magnetBonusRange = magnetBonusRange;
        this.reloadMultiplier = reloadMultiplier;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public int cost() {
        return cost;
    }

    public String texturePath() {
        return texturePath;
    }

    public float oxygenBonus() {
        return oxygenBonus;
    }

    public float agilityMultiplier() {
        return agilityMultiplier;
    }

    public float magnetBonusRange() {
        return magnetBonusRange;
    }

    public float reloadMultiplier() {
        return reloadMultiplier;
    }
}
