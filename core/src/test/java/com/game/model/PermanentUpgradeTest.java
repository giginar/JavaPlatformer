package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermanentUpgradeTest {
    @Test
    void permanentUpgradeCostsGrowWithPurchasedLevels() {
        PermanentUpgrade upgrade = PermanentUpgrade.PRESSURE_TANK;

        assertEquals(8, upgrade.costForLevel(0));
        assertEquals(16, upgrade.costForLevel(1));
        assertEquals(24, upgrade.costForLevel(2));
    }

    @Test
    void everyEquipmentUpgradeHasThreeLevelsTakingFiveTenAndFifteenMinutes() {
        for (PermanentUpgrade upgrade : PermanentUpgrade.values()) {
            assertEquals(3, upgrade.maxLevel());
            assertEquals(300_000L, upgrade.installDurationMillis(1));
            assertEquals(600_000L, upgrade.installDurationMillis(2));
            assertEquals(900_000L, upgrade.installDurationMillis(3));
            assertThrows(IllegalArgumentException.class, () -> upgrade.installDurationMillis(0));
            assertThrows(IllegalArgumentException.class, () -> upgrade.installDurationMillis(4));
        }
    }
}
