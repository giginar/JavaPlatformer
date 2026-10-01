package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardedRequestGateTest {
    @Test
    void repeatedTapsOwnOnlyOneRequestUntilClose() {
        RewardedRequestGate gate = new RewardedRequestGate();

        assertTrue(gate.tryBegin());
        assertFalse(gate.tryBegin());
        assertTrue(gate.isInFlight());
        gate.finish();
        assertTrue(gate.tryBegin());
    }

    @Test
    void showFailureRestoresTheCta() {
        RewardedRequestGate gate = new RewardedRequestGate();

        assertTrue(gate.tryBegin());
        gate.finish();

        assertFalse(gate.isInFlight());
        assertTrue(gate.tryBegin());
    }
}
