package com.game.ads;

/** Mutual-exclusion token gate that rejects stale full-screen callbacks. */
public final class FullScreenAdGate {
    public static final long REJECTED = 0L;

    private long nextToken = 1L;
    private long activeToken = REJECTED;

    public synchronized long tryBegin() {
        if (activeToken != REJECTED) {
            return REJECTED;
        }
        activeToken = nextToken++;
        if (nextToken == REJECTED) {
            nextToken++;
        }
        return activeToken;
    }

    public synchronized boolean isCurrent(long token) {
        return token != REJECTED && token == activeToken;
    }

    public synchronized boolean finish(long token) {
        if (!isCurrent(token)) {
            return false;
        }
        activeToken = REJECTED;
        return true;
    }

    public synchronized boolean recoverAfterLifecycleLoss() {
        if (activeToken == REJECTED) {
            return false;
        }
        activeToken = REJECTED;
        return true;
    }

    public synchronized boolean isActive() {
        return activeToken != REJECTED;
    }
}
