package com.game.ads;

import java.util.Objects;
import java.util.regex.Pattern;

/** Validated build-time advertising identifiers and operating mode. */
public final class AdConfiguration {
    public static final String TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713";
    public static final String TEST_REWARDED_AD_UNIT_ID =
        "ca-app-pub-3940256099942544/5224354917";
    public static final String TEST_INTERSTITIAL_AD_UNIT_ID =
        "ca-app-pub-3940256099942544/1033173712";

    private static final Pattern APP_ID = Pattern.compile("ca-app-pub-[0-9]{16}~[0-9]{10}");
    private static final Pattern AD_UNIT_ID = Pattern.compile("ca-app-pub-[0-9]{16}/[0-9]{10}");

    private final AdMode mode;
    private final String appId;
    private final String rewardedAdUnitId;
    private final String interstitialAdUnitId;

    private AdConfiguration(AdMode mode, String appId, String rewardedAdUnitId,
                            String interstitialAdUnitId) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.appId = appId;
        this.rewardedAdUnitId = rewardedAdUnitId;
        this.interstitialAdUnitId = interstitialAdUnitId;
    }

    public static AdConfiguration disabled() {
        return new AdConfiguration(AdMode.DISABLED, "", "", "");
    }

    public static AdConfiguration test() {
        return new AdConfiguration(AdMode.TEST, TEST_APP_ID,
            TEST_REWARDED_AD_UNIT_ID, TEST_INTERSTITIAL_AD_UNIT_ID);
    }

    public static AdConfiguration production(String appId, String rewardedAdUnitId,
                                               String interstitialAdUnitId) {
        String validatedAppId = requireFormat("production App ID", appId, APP_ID);
        String validatedRewarded = requireFormat(
            "production rewarded ad unit ID", rewardedAdUnitId, AD_UNIT_ID);
        String validatedInterstitial = requireFormat(
            "production interstitial ad unit ID", interstitialAdUnitId, AD_UNIT_ID);
        rejectDemoId("production App ID", validatedAppId);
        rejectDemoId("production rewarded ad unit ID", validatedRewarded);
        rejectDemoId("production interstitial ad unit ID", validatedInterstitial);
        return new AdConfiguration(AdMode.PRODUCTION, validatedAppId,
            validatedRewarded, validatedInterstitial);
    }

    public AdMode mode() {
        return mode;
    }

    public boolean adsEnabled() {
        return mode != AdMode.DISABLED;
    }

    public String appId() {
        return appId;
    }

    public String rewardedAdUnitId() {
        return rewardedAdUnitId;
    }

    public String interstitialAdUnitId() {
        return interstitialAdUnitId;
    }

    private static String requireFormat(String name, String value, Pattern pattern) {
        String trimmed = Objects.requireNonNull(value, name).trim();
        if (!pattern.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(name + " is missing or invalid");
        }
        return trimmed;
    }

    private static void rejectDemoId(String name, String value) {
        if (value.equals(TEST_APP_ID) || value.equals(TEST_REWARDED_AD_UNIT_ID)
            || value.equals(TEST_INTERSTITIAL_AD_UNIT_ID)) {
            throw new IllegalArgumentException(name + " must not use a Google demo ID");
        }
    }
}
