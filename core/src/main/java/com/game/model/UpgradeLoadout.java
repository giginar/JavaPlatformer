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
        if (currentLevel >= type.maxLevel()) {
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
            if (level(type) < type.maxLevel()) {
                available.add(type);
            }
        }
        return available;
    }

    public float oxygenDrainMultiplier() {
        return Math.max(0.5f, 1f - level(UpgradeType.OXYGEN_EFFICIENCY) * 0.15f);
    }

    public float shootCooldownMultiplier() {
        return Math.max(0.45f, 1f - level(UpgradeType.RAPID_FIRE) * 0.18f);
    }

    public int harpoonHitCount() {
        return 1 + level(UpgradeType.PIERCING_HARPOON);
    }

    public float oxygenPickupBonus() {
        return level(UpgradeType.LARGE_TANKS) * 10f;
    }

    public float agilityMultiplier() {
        return 1f + level(UpgradeType.AGILE_DIVER) * 0.12f;
    }
}
