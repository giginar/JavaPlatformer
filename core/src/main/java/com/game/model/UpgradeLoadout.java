package com.game.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public final class UpgradeLoadout {
    private final EnumMap<UpgradeType, Integer> levels = new EnumMap<>(UpgradeType.class);

    public UpgradeLoadout() {
        reset();
    }

    public void reset() {
        levels.clear();
        for (UpgradeType type : UpgradeType.values()) {
            levels.put(type, 0);
        }
    }

    public boolean apply(UpgradeType type) {
        int currentLevel = level(type);
        if (type.isMaxed(currentLevel)) {
            return false;
        }
        levels.put(type, currentLevel + 1);
        return true;
    }

    public int level(UpgradeType type) {
        return levels.getOrDefault(type, 0);
    }

    public List<UpgradeType> availableUpgrades() {
        List<UpgradeType> available = new ArrayList<>();
        for (UpgradeType type : UpgradeType.values()) {
            if (!type.isMaxed(level(type))) {
                available.add(type);
            }
        }
        return available;
    }

    public float oxygenDrainMultiplier() {
        return oxygenDrainMultiplier(level(UpgradeType.OXYGEN_EFFICIENCY));
    }

    public float oxygenDrainMultiplierAfterNextLevel() {
        return oxygenDrainMultiplier(level(UpgradeType.OXYGEN_EFFICIENCY) + 1);
    }

    public float shootCooldownMultiplier() {
        return shootCooldownMultiplier(level(UpgradeType.RAPID_FIRE));
    }

    public float shootCooldownMultiplierAfterNextLevel() {
        return shootCooldownMultiplier(level(UpgradeType.RAPID_FIRE) + 1);
    }

    public int harpoonHitCount() {
        return 1 + level(UpgradeType.PIERCING_HARPOON);
    }

    public float oxygenPickupBonus() {
        return oxygenPickupBonus(level(UpgradeType.LARGE_TANKS));
    }

    public float oxygenPickupBonusAfterNextLevel() {
        return oxygenPickupBonus(level(UpgradeType.LARGE_TANKS) + 1);
    }

    public float agilityMultiplier() {
        return 1f + level(UpgradeType.AGILE_DIVER) * 0.12f;
    }

    private static float oxygenDrainMultiplier(int level) {
        return Math.max(0.5f, 1f - level * 0.15f);
    }

    private static float shootCooldownMultiplier(int level) {
        return Math.max(0.45f, 1f - level * 0.18f);
    }

    private static float oxygenPickupBonus(int level) {
        return level * 10f;
    }
}
