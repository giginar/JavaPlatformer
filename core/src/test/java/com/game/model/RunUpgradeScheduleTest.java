package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunUpgradeScheduleTest {
    @Test
    void openingThresholdsContinueAtRegularIntervals() {
        assertEquals(1000f, RunUpgradeSchedule.scoreForSelection(0));
        assertEquals(2500f, RunUpgradeSchedule.scoreForSelection(1));
        assertEquals(5000f, RunUpgradeSchedule.scoreForSelection(2));
        assertEquals(7500f, RunUpgradeSchedule.scoreForSelection(3));
        assertEquals(10000f, RunUpgradeSchedule.scoreForSelection(4));
        assertEquals(250000f, RunUpgradeSchedule.scoreForSelection(100));
    }

    @Test
    void negativeSelectionIndexesAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> RunUpgradeSchedule.scoreForSelection(-1));
    }
}
