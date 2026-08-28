package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermanentUpgradeTest {
    @Test
    void permanentUpgradeCostsGrowWithPurchasedLevels() {
        PermanentUpgrade upgrade = PermanentUpgrade.PRESSURE_TANK;

        assertEquals(8, upgrade.costForLevel(0));
        assertEquals(16, upgrade.costForLevel(1));
        assertEquals(32, upgrade.costForLevel(3));
        assertEquals(4, upgrade.maxLevel());
    }
}
