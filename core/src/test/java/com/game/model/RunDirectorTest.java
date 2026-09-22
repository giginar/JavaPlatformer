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

        assertTrue(director.update(10f).stageChanged());
        assertEquals(3, director.stage());
        assertTrue(director.update(10f).stageChanged());
        assertEquals(4, director.stage());
        assertTrue(director.update(10f).stageChanged());
        assertEquals(5, director.stage());
        RunDirector.UpdateResult finale = director.update(10f);
        assertTrue(finale.finaleReady());
        assertFalse(director.update(1f).finaleReady());
    }

    @Test
    void aLargeUpdateCannotSkipStageCompletionBoundaries() {
        RunDirector director = new RunDirector(10f);

        assertTrue(director.update(50f).stageChanged());
        assertEquals(2, director.stage());
        assertFalse(director.finaleReady());

        for (int expectedStage = 3; expectedStage <= 5; expectedStage++) {
            assertTrue(director.update(0.01f).stageChanged());
            assertEquals(expectedStage, director.stage());
        }
        assertTrue(director.finaleReady());
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

    @Test
    void captureJumpStartsInsideRequestedStageWithoutStartingFinale() {
        RunDirector director = new RunDirector(10f);

        director.jumpToStageForCapture(5);

        assertEquals(5, director.stage());
        assertEquals(0f, director.stageProgress(), 0.0001f);
        assertFalse(director.finaleReady());
        assertFalse(director.update(0.1f).finaleReady());
    }
}
