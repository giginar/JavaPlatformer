package com.game.ads;

/** Desktop and disabled fallback. It never initializes an advertising SDK. */
public final class NoOpAdvertisingService implements AdvertisingService {
    @Override
    public boolean canRequestAds() {
        return false;
    }

    @Override
    public boolean isPrivacyOptionsRequired() {
        return false;
    }

    @Override
    public boolean isRewardedAvailable() {
        return false;
    }

    @Override
    public boolean isInterstitialAvailable() {
        return false;
    }

    @Override
    public boolean isFullScreenContentActive() {
        return false;
    }

    @Override
    public boolean showPrivacyOptions(FullScreenCallback callback) {
        return false;
    }

    @Override
    public boolean showRewarded(RewardedCallback callback) {
        return false;
    }

    @Override
    public boolean showInterstitial(FullScreenCallback callback) {
        return false;
    }

    @Override
    public void dispose() {
        // Nothing to release.
    }
}
