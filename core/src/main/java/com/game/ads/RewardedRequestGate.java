package com.game.ads;

/** Owns one rewarded show request until it closes or fails. */
public final class RewardedRequestGate {
    private boolean inFlight;

    public synchronized boolean tryBegin() {
        if (inFlight) {
            return false;
        }
        inFlight = true;
        return true;
    }

    public synchronized void finish() {
        inFlight = false;
    }

    public synchronized boolean isInFlight() {
        return inFlight;
    }
}
