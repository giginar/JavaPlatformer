package com.game.model;

/** Completed equipment bonuses captured at the start of a dive. */
public record EquipmentLoadout(float startingMaxOxygen, float startingShieldSeconds,
                               float magnetBonusRange, int startingHarpoonBonus,
                               float pearlRewardMultiplier) {
    public static final EquipmentLoadout NONE = new EquipmentLoadout(
        GameSession.MAX_OXYGEN, 0f, 0f, 0, 1f);
}
