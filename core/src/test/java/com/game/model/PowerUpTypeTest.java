package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerUpTypeTest {
    @Test
    void everyPowerUpExplainsHowItIsActivatedOrUsed() {
        for (PowerUpType type : PowerUpType.values()) {
            assertFalse(type.usageHint().isBlank(), type.name());
        }
    }

    @Test
    void dashIsStoredWhileOtherPowerUpsActivateOnPickup() {
        assertFalse(PowerUpType.TORPEDO_DASH.activatesOnPickup());
        for (PowerUpType type : PowerUpType.values()) {
            if (type != PowerUpType.TORPEDO_DASH) {
                assertTrue(type.activatesOnPickup(), type.name());
            }
        }
    }
}
