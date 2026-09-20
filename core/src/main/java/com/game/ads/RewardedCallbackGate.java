package com.game.ads;

import java.util.Objects;

/** Delivers SDK full-screen and reward callbacks at most once for one ad show. */
public final class RewardedCallbackGate {
    private final AdvertisingService.RewardedCallback callback;
    private boolean opened;
    private boolean rewardDelivered;
    private boolean closed;
    private boolean failed;

    public RewardedCallbackGate(AdvertisingService.RewardedCallback callback) {
        this.callback = Objects.requireNonNull(callback, "callback");
    }

    public synchronized void opened() {
        if (!opened && !closed && !failed) {
            opened = true;
            callback.onOpened();
        }
    }

    public synchronized void rewardEarned() {
        if (!rewardDelivered && !failed) {
            rewardDelivered = true;
            callback.onRewardEarned();
        }
    }

    public synchronized void dismissed() {
        closeOnce();
    }

    public synchronized void failed() {
        failed = true;
        closeOnce();
    }

    private void closeOnce() {
        if (!closed) {
            closed = true;
            callback.onClosed();
        }
    }
}
