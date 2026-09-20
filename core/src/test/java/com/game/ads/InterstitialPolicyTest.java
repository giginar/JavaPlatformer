package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterstitialPolicyTest {
    @Test
    void suppressesFirstOpportunityAndAllowsOnlyAfterThreeCompletedRuns() {
        InterstitialPolicy policy = new InterstitialPolicy();

        policy.recordCompletedRun();
        assertFalse(eligible(policy, 1L, 0L));
        policy.recordCompletedRun();
        assertFalse(eligible(policy, 2L, 0L));
        policy.recordCompletedRun();
        assertTrue(eligible(policy, 3L, 0L));
    }

    @Test
    void enforcesTenMinuteCooldownAndThreeMoreRunsAfterDisplay() {
        InterstitialPolicy policy = readyPolicy();
        assertTrue(eligible(policy, 1L, 1_000L));
        policy.recordInterstitialDisplay(1_000L);

        policy.recordCompletedRun();
        policy.recordCompletedRun();
        policy.recordCompletedRun();
        assertFalse(eligible(policy, 2L,
            1_000L + InterstitialPolicy.MIN_INTERVAL_MILLIS - 1L));
        assertTrue(eligible(policy, 3L,
            1_000L + InterstitialPolicy.MIN_INTERVAL_MILLIS));
    }

    @Test
    void rewardedDisplaySuppressesTheNextResultsTransition() {
        InterstitialPolicy policy = readyPolicy();
        policy.recordRewardedDisplay();

        assertFalse(eligible(policy, 1L, 0L));
        assertTrue(eligible(policy, 2L, 0L));
    }

    @Test
    void rejectsDuplicateOpportunityAndActiveGameplay() {
        InterstitialPolicy policy = readyPolicy();

        assertFalse(policy.evaluateResultsToMenuOpportunity(
            1L, 0L, true, true, true, false));
        assertFalse(eligible(policy, 1L, 0L));
        assertTrue(eligible(policy, 2L, 0L));
    }

    @Test
    void requiresConsentAvailabilityAndNoOtherFullScreenContent() {
        InterstitialPolicy policy = readyPolicy();

        assertFalse(policy.evaluateResultsToMenuOpportunity(
            1L, 0L, false, false, true, false));
        assertFalse(policy.evaluateResultsToMenuOpportunity(
            2L, 0L, false, true, false, false));
        assertFalse(policy.evaluateResultsToMenuOpportunity(
            3L, 0L, false, true, true, true));
    }

    private static InterstitialPolicy readyPolicy() {
        InterstitialPolicy policy = new InterstitialPolicy();
        policy.recordCompletedRun();
        policy.recordCompletedRun();
        policy.recordCompletedRun();
        return policy;
    }

    private static boolean eligible(InterstitialPolicy policy, long opportunityId,
                                    long nowMillis) {
        return policy.evaluateResultsToMenuOpportunity(
            opportunityId, nowMillis, false, true, true, false);
    }
}
