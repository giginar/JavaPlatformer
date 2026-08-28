package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeLoadoutTest {
    @Test
    void upgradesChangeTheirDerivedGameplayStats() {
        UpgradeLoadout loadout = new UpgradeLoadout();

        loadout.apply(UpgradeType.RAPID_FIRE);
        loadout.apply(UpgradeType.PIERCING_HARPOON);
        loadout.apply(UpgradeType.OXYGEN_EFFICIENCY);
        loadout.apply(UpgradeType.LARGE_TANKS);
        loadout.apply(UpgradeType.AGILE_DIVER);

        assertEquals(0.82f, loadout.shootCooldownMultiplier(), 0.0001f);
        assertEquals(2, loadout.harpoonHitCount());
        assertEquals(0.85f, loadout.oxygenDrainMultiplier(), 0.0001f);
        assertEquals(10f, loadout.oxygenPickupBonus(), 0.0001f);
        assertEquals(1.12f, loadout.agilityMultiplier(), 0.0001f);
    }

    @Test
    void upgradeLevelsRemainAvailableBeyondTheFormerCap() {
        UpgradeLoadout loadout = new UpgradeLoadout();

        for (int level = 0; level < 20; level++) {
            assertTrue(loadout.apply(UpgradeType.RAPID_FIRE));
        }

        assertEquals(20, loadout.level(UpgradeType.RAPID_FIRE));
        assertTrue(loadout.availableUpgrades().contains(UpgradeType.RAPID_FIRE));
    }

    @Test
    void resetRemovesEveryUpgrade() {
        UpgradeLoadout loadout = new UpgradeLoadout();
        for (UpgradeType type : UpgradeType.values()) {
            loadout.apply(type);
        }

        loadout.reset();

        for (UpgradeType type : UpgradeType.values()) {
            assertEquals(0, loadout.level(type));
        }
        assertEquals(UpgradeType.values().length, loadout.availableUpgrades().size());
    }
}
