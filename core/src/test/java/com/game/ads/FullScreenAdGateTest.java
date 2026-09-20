package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullScreenAdGateTest {
    @Test
    void permitsOnlyOneFullScreenOperationAndRejectsStaleCompletion() {
        FullScreenAdGate gate = new FullScreenAdGate();
        long first = gate.tryBegin();

        assertTrue(first != FullScreenAdGate.REJECTED);
        assertTrue(gate.isActive());
        assertTrue(gate.tryBegin() == FullScreenAdGate.REJECTED);
        assertFalse(gate.finish(first + 1));
        assertTrue(gate.finish(first));
        assertFalse(gate.isActive());
    }

    @Test
    void lifecycleRecoveryClearsAnAbandonedOperation() {
        FullScreenAdGate gate = new FullScreenAdGate();
        gate.tryBegin();

        assertTrue(gate.recoverAfterLifecycleLoss());
        assertFalse(gate.recoverAfterLifecycleLoss());
        assertFalse(gate.isActive());
        assertTrue(gate.tryBegin() != FullScreenAdGate.REJECTED);
    }
}
