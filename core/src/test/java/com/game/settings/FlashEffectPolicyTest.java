package com.game.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlashEffectPolicyTest {
    @Test
    void disabledFlashesUseTheSteadyBaseValue() {
        assertEquals(0.6f, FlashEffectPolicy.pulse(false, 0.6f, 0.35f, 1.2f));
        assertFalse(FlashEffectPolicy.blink(false, true, true));
    }

    @Test
    void enabledFlashesPreserveAuthoredFeedback() {
        assertTrue(FlashEffectPolicy.pulse(true, 0.6f, 0.35f, 1.2f) > 0.6f);
        assertTrue(FlashEffectPolicy.blink(true, true, true));
        assertFalse(FlashEffectPolicy.blink(true, false, true));
    }
}
