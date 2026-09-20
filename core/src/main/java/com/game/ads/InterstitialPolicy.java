package com.game.ads;

/** Session-local sparse interstitial policy evaluated only at a completed-results transition. */
public final class InterstitialPolicy {
    public static final int MIN_COMPLETED_RUNS = 3;
    public static final long MIN_INTERVAL_MILLIS = 10L * 60L * 1000L;

    private int completedRunsSinceDisplay;
    private long lastDisplayMillis = -1L;
    private long lastOpportunityId = Long.MIN_VALUE;
    private boolean suppressNextOpportunityAfterRewarded;

    public synchronized void recordCompletedRun() {
        if (completedRunsSinceDisplay < Integer.MAX_VALUE) {
            completedRunsSinceDisplay++;
        }
    }

    public synchronized void recordRewardedDisplay() {
        suppressNextOpportunityAfterRewarded = true;
    }

    public synchronized boolean evaluateResultsToMenuOpportunity(
        long opportunityId,
        long nowMillis,
        boolean activeGameplay,
        boolean canRequestAds,
        boolean interstitialAvailable,
        boolean fullScreenContentActive
    ) {
        if (opportunityId == lastOpportunityId) {
            return false;
        }
        lastOpportunityId = opportunityId;

        if (suppressNextOpportunityAfterRewarded) {
            suppressNextOpportunityAfterRewarded = false;
            return false;
        }
        if (activeGameplay || !canRequestAds || !interstitialAvailable
            || fullScreenContentActive || completedRunsSinceDisplay < MIN_COMPLETED_RUNS) {
            return false;
        }
        return lastDisplayMillis < 0L
            || nowMillis >= lastDisplayMillis
            && nowMillis - lastDisplayMillis >= MIN_INTERVAL_MILLIS;
    }

    public synchronized void recordInterstitialDisplay(long nowMillis) {
        lastDisplayMillis = nowMillis;
        completedRunsSinceDisplay = 0;
    }

    int completedRunsSinceDisplay() {
        return completedRunsSinceDisplay;
    }
}
