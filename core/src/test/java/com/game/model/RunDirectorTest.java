package com.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunDirectorTest {
    @Test
    void stagesChangeAtConfiguredTimeAndFinaleFollowsStageFive() {
        RunDirector director = new RunDirector(10f);

        assertFalse(director.update(9.99f).stageChanged());
        assertEquals(1, director.stage());
        assertTrue(director.update(0.01f).stageChanged());
        assertEquals(2, director.stage());

        RunDirector.UpdateResult finale = director.update(40f);
        assertEquals(5, director.stage());
        assertTrue(finale.finaleReady());
    }

    @Test
    void distanceUsesDiverScaleAndReportsTheFirstMilestone() {
        RunDirector oneStep = new RunDirector(600f);
        RunDirector manySteps = new RunDirector(600f);

        RunDirector.UpdateResult result = oneStep.update(194f);
        for (int i = 0; i < 1940; i++) {
            manySteps.update(0.1f);
        }

        assertEquals(oneStep.distanceMeters(), manySteps.distanceMeters(), 0.1f);
        assertEquals(500, result.milestoneMeters());
        assertEquals(1000, oneStep.nextMilestoneMeters());
    }
}
