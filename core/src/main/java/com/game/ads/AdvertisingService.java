package com.game.ads;

/** Platform boundary for optional advertising and Google's privacy-options UI. */
public interface AdvertisingService {
    interface FullScreenCallback {
        void onOpened();

        void onClosed();
    }

    interface RewardedCallback extends FullScreenCallback {
        void onRewardEarned();
    }

    boolean canRequestAds();

    boolean isPrivacyOptionsRequired();

    boolean isRewardedAvailable();

    boolean isInterstitialAvailable();

    boolean isFullScreenContentActive();

    boolean showPrivacyOptions(FullScreenCallback callback);

    boolean showRewarded(RewardedCallback callback);

    boolean showInterstitial(FullScreenCallback callback);

    void dispose();
}
