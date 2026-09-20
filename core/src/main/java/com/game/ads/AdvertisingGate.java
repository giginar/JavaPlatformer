package com.game.ads;

/** Thread-safe consent and one-time initialization gate used by platform implementations. */
public final class AdvertisingGate {
    private final boolean enabled;
    private boolean canRequestAds;
    private boolean privacyOptionsRequired;
    private boolean initializationStarted;
    private boolean initialized;

    public AdvertisingGate(boolean enabled) {
        this.enabled = enabled;
    }

    public synchronized void updateConsent(boolean canRequestAds,
                                           boolean privacyOptionsRequired) {
        this.canRequestAds = enabled && canRequestAds;
        this.privacyOptionsRequired = enabled && privacyOptionsRequired;
    }

    public synchronized boolean tryBeginInitialization() {
        if (!enabled || !canRequestAds || initializationStarted) {
            return false;
        }
        initializationStarted = true;
        return true;
    }

    public synchronized void markInitialized() {
        if (initializationStarted) {
            initialized = true;
        }
    }

    public synchronized void markInitializationFailed() {
        if (!initialized) {
            initializationStarted = false;
        }
    }

    public synchronized boolean canRequestAds() {
        return canRequestAds;
    }

    public synchronized boolean canLoadAds() {
        return canRequestAds && initialized;
    }

    public synchronized boolean isPrivacyOptionsRequired() {
        return privacyOptionsRequired;
    }

    public synchronized boolean initializationStarted() {
        return initializationStarted;
    }
}
