package com.game.diver.android;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.game.ads.AdCache;
import com.game.ads.AdConfiguration;
import com.game.ads.AdMode;
import com.game.ads.AdvertisingGate;
import com.game.ads.AdvertisingService;
import com.game.ads.FullScreenAdGate;
import com.game.ads.RewardedCallbackGate;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Android-only UMP and GMA Next-Gen implementation. */
public final class AndroidAdvertisingService implements AdvertisingService {
    private static final String TAG = "DeepDriftAds";
    private static final String QA_UMP_GEOGRAPHY_EXTRA = "deepdive.qa.ump_geography";
    private static final String QA_UMP_RESET_EXTRA = "deepdive.qa.ump_reset";
    private static final String QA_UMP_TEST_DEVICE_EXTRA = "deepdive.qa.ump_test_device";

    private final AdConfiguration configuration;
    private final AdvertisingGate advertisingGate;
    private final FullScreenAdGate fullScreenGate = new FullScreenAdGate();
    private final AdCache<RewardedAd> rewardedCache = new AdCache<>();
    private final AdCache<InterstitialAd> interstitialCache = new AdCache<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean disposed = new AtomicBoolean();
    private final AtomicBoolean consentRefreshStarted = new AtomicBoolean();
    private final AtomicBoolean consentFormPending = new AtomicBoolean();
    private final AtomicBoolean consentFormRetryScheduled = new AtomicBoolean();

    private volatile WeakReference<Activity> activityReference;
    private volatile ConsentInformation consentInformation;

    public AndroidAdvertisingService(Activity activity, AdConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        advertisingGate = new AdvertisingGate(configuration.adsEnabled());
        attachActivity(activity);

        if (configuration.mode() == AdMode.TEST) {
            Log.i(TAG, "TEST mode: Google demo ad identifiers only");
        } else if (configuration.mode() == AdMode.PRODUCTION) {
            Log.i(TAG, "PRODUCTION mode: externally supplied ad identifiers");
        } else {
            Log.i(TAG, "DISABLED mode: no consent, initialization, or ad requests");
        }
        Log.i(TAG, "POLICY CONFIGURATION PENDING: no age or child-directed flags set");
    }

    public void start() {
        if (!configuration.adsEnabled() || !consentRefreshStarted.compareAndSet(false, true)) {
            return;
        }
        runOnActivity(activity -> {
            try {
                consentInformation = UserMessagingPlatform.getConsentInformation(activity);
                if (configuration.mode() == AdMode.TEST
                    && activity.getIntent().getBooleanExtra(QA_UMP_RESET_EXTRA, false)) {
                    consentInformation.reset();
                    Log.i(TAG, "UMP TEST state reset through the development QA gate");
                }
                ConsentRequestParameters.Builder parameterBuilder =
                    new ConsentRequestParameters.Builder()
                        .setAdMobAppId(configuration.appId());
                applyTestConsentDebugSettings(activity, parameterBuilder);
                // POLICY CONFIGURATION PENDING: do not set age-related flags here.
                ConsentRequestParameters parameters = parameterBuilder.build();
                consentInformation.requestConsentInfoUpdate(
                    activity,
                    parameters,
                    () -> loadRequiredConsentForm(activity),
                    error -> {
                        Log.w(TAG, "UMP consent information update failed: " + error.getMessage());
                        updateConsentStateAndAds();
                    }
                );

                // UMP permits using a valid consent state retained from a previous launch.
                updateConsentStateAndAds();
            } catch (RuntimeException error) {
                Log.e(TAG, "UMP consent refresh threw", error);
                updateConsentStateAndAds();
            }
        }, () -> {
            consentRefreshStarted.set(false);
            Log.w(TAG, "UMP consent refresh deferred: Activity unavailable");
        });
    }

    private void applyTestConsentDebugSettings(Activity activity,
                                                ConsentRequestParameters.Builder parameters) {
        if (configuration.mode() != AdMode.TEST) {
            return;
        }
        int geography = activity.getIntent().getIntExtra(QA_UMP_GEOGRAPHY_EXTRA,
            ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED);
        String testDevice = activity.getIntent().getStringExtra(QA_UMP_TEST_DEVICE_EXTRA);
        if (geography == ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED
            || testDevice == null || testDevice.isBlank()) {
            return;
        }
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
            .addTestDeviceHashedId(testDevice)
            .setDebugGeography(geography)
            .setForceTesting(true)
            .build();
        parameters.setConsentDebugSettings(debugSettings);
        Log.i(TAG, "UMP TEST debug geography enabled: " + geography);
    }

    public void attachActivity(Activity activity) {
        activityReference = new WeakReference<>(Objects.requireNonNull(activity, "activity"));
    }

    public void detachActivity(Activity activity) {
        WeakReference<Activity> reference = activityReference;
        if (reference != null && reference.get() == activity) {
            reference.clear();
        }
    }

    public void onActivityResumed(Activity activity) {
        attachActivity(activity);
        start();
        if (consentFormPending.get()) {
            loadRequiredConsentForm(activity);
            return;
        }
        if (advertisingGate.canLoadAds()) {
            runOnActivity(ignored -> loadAdsIfAllowed(), null);
        }
    }

    @Override
    public boolean canRequestAds() {
        return advertisingGate.canRequestAds();
    }

    @Override
    public boolean isPrivacyOptionsRequired() {
        return advertisingGate.isPrivacyOptionsRequired();
    }

    @Override
    public boolean isRewardedAvailable() {
        return !consentFormPending.get() && advertisingGate.canLoadAds()
            && rewardedCache.isAvailable()
            && !fullScreenGate.isActive();
    }

    @Override
    public boolean isInterstitialAvailable() {
        return !consentFormPending.get() && advertisingGate.canLoadAds()
            && interstitialCache.isAvailable()
            && !fullScreenGate.isActive();
    }

    @Override
    public boolean isFullScreenContentActive() {
        return fullScreenGate.isActive();
    }

    @Override
    public boolean showPrivacyOptions(FullScreenCallback callback) {
        Objects.requireNonNull(callback, "callback");
        if (!isPrivacyOptionsRequired() || disposed.get()) {
            return false;
        }
        Activity activity = currentActivity();
        if (activity == null) {
            Log.w(TAG, "Privacy options unavailable: Activity missing");
            return false;
        }
        long token = fullScreenGate.tryBegin();
        if (token == FullScreenAdGate.REJECTED) {
            return false;
        }

        AtomicBoolean closed = new AtomicBoolean();
        activity.runOnUiThread(() -> {
            if (!isUsable(activity)) {
                finishFullScreen(token, closed, callback, "Privacy options Activity lost");
                return;
            }
            callback.onOpened();
            try {
                UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
                    if (formError != null) {
                        Log.w(TAG, "UMP privacy options failed: " + formError.getMessage());
                    }
                    updateConsentStateAndAds();
                    finishFullScreen(token, closed, callback, null);
                });
            } catch (RuntimeException error) {
                Log.e(TAG, "UMP privacy options threw", error);
                finishFullScreen(token, closed, callback, null);
            }
        });
        return true;
    }

    @Override
    public boolean showRewarded(RewardedCallback callback) {
        Objects.requireNonNull(callback, "callback");
        if (consentFormPending.get() || !advertisingGate.canLoadAds() || disposed.get()) {
            return false;
        }
        Activity activity = currentActivity();
        if (activity == null) {
            Log.w(TAG, "Rewarded ad unavailable: Activity missing");
            return false;
        }
        long token = fullScreenGate.tryBegin();
        if (token == FullScreenAdGate.REJECTED) {
            return false;
        }
        RewardedAd ad = rewardedCache.take();
        if (ad == null) {
            fullScreenGate.finish(token);
            return false;
        }
        RewardedCallbackGate callbackGate = new RewardedCallbackGate(callback);

        activity.runOnUiThread(() -> showRewardedOnMainThread(
            activity, ad, token, callbackGate));
        return true;
    }

    @Override
    public boolean showInterstitial(FullScreenCallback callback) {
        Objects.requireNonNull(callback, "callback");
        if (consentFormPending.get() || !advertisingGate.canLoadAds() || disposed.get()) {
            return false;
        }
        Activity activity = currentActivity();
        if (activity == null) {
            Log.w(TAG, "Interstitial unavailable: Activity missing");
            return false;
        }
        long token = fullScreenGate.tryBegin();
        if (token == FullScreenAdGate.REJECTED) {
            return false;
        }
        InterstitialAd ad = interstitialCache.take();
        if (ad == null) {
            fullScreenGate.finish(token);
            return false;
        }
        AtomicBoolean opened = new AtomicBoolean();
        AtomicBoolean closed = new AtomicBoolean();

        activity.runOnUiThread(() -> showInterstitialOnMainThread(
            activity, ad, token, opened, closed, callback));
        return true;
    }

    @Override
    public void dispose() {
        if (!disposed.compareAndSet(false, true)) {
            return;
        }
        mainHandler.removeCallbacksAndMessages(null);
        consentFormPending.set(false);
        fullScreenGate.recoverAfterLifecycleLoss();
        RewardedAd rewarded = rewardedCache.clear();
        InterstitialAd interstitial = interstitialCache.clear();
        Activity activity = currentActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (rewarded != null) rewarded.destroy();
                if (interstitial != null) interstitial.destroy();
            });
        }
        WeakReference<Activity> reference = activityReference;
        if (reference != null) reference.clear();
    }

    private void loadRequiredConsentForm(Activity activity) {
        if (disposed.get()) {
            return;
        }
        consentFormPending.set(true);
        if (!isUsable(activity)) {
            scheduleConsentFormRetry();
            return;
        }
        long token = fullScreenGate.tryBegin();
        if (token == FullScreenAdGate.REJECTED) {
            scheduleConsentFormRetry();
            return;
        }
        try {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, formError -> {
                if (formError != null) {
                    Log.w(TAG, "UMP required consent form failed: " + formError.getMessage());
                }
                consentFormPending.set(false);
                fullScreenGate.finish(token);
                updateConsentStateAndAds();
            });
        } catch (RuntimeException error) {
            consentFormPending.set(false);
            fullScreenGate.finish(token);
            Log.e(TAG, "UMP required consent form threw", error);
            updateConsentStateAndAds();
        }
    }

    private void scheduleConsentFormRetry() {
        if (!consentFormRetryScheduled.compareAndSet(false, true)) {
            return;
        }
        mainHandler.postDelayed(() -> {
            consentFormRetryScheduled.set(false);
            if (!consentFormPending.get() || disposed.get()) {
                return;
            }
            Activity activity = currentActivity();
            if (activity != null) {
                loadRequiredConsentForm(activity);
            }
        }, 250L);
    }

    private void updateConsentStateAndAds() {
        ConsentInformation information = consentInformation;
        if (information == null || disposed.get()) {
            return;
        }
        boolean privacyRequired = information.getPrivacyOptionsRequirementStatus()
            == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
        advertisingGate.updateConsent(information.canRequestAds(), privacyRequired);
        if (!advertisingGate.canRequestAds()) {
            clearCachedAds();
            return;
        }
        initializeAdsOnce();
        loadAdsIfAllowed();
    }

    private void initializeAdsOnce() {
        if (!advertisingGate.tryBeginInitialization()) {
            return;
        }
        runOnActivity(activity -> {
            try {
                InitializationConfig initializationConfig = new InitializationConfig.Builder(
                    configuration.appId())
                    .disableSdkCrashReporting()
                    .build();
                MobileAds.initialize(activity.getApplicationContext(), initializationConfig,
                    ignored -> {
                        advertisingGate.markInitialized();
                        Log.i(TAG, "GMA Next-Gen initialized in " + configuration.mode() + " mode");
                        loadAdsIfAllowed();
                    });
            } catch (RuntimeException error) {
                advertisingGate.markInitializationFailed();
                Log.e(TAG, "GMA Next-Gen initialization failed", error);
            }
        }, () -> {
            advertisingGate.markInitializationFailed();
            Log.w(TAG, "GMA initialization deferred: Activity unavailable");
        });
    }

    private void loadAdsIfAllowed() {
        runOnActivity(ignored -> loadAdsOnMainThread(), null);
    }

    private void loadAdsOnMainThread() {
        if (consentFormPending.get() || !advertisingGate.canLoadAds()
            || disposed.get() || fullScreenGate.isActive()) {
            return;
        }
        loadRewarded();
        loadInterstitial();
    }

    private void loadRewarded() {
        long loadToken = rewardedCache.tryBeginLoad();
        if (loadToken == AdCache.REJECTED) {
            return;
        }
        try {
            RewardedAd.load(
                new AdRequest.Builder(configuration.rewardedAdUnitId()).build(),
                new AdLoadCallback<>() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        if (!rewardedCache.complete(loadToken, ad) || disposed.get()
                            || !advertisingGate.canRequestAds()) {
                            ad.destroy();
                            rewardedCache.clear();
                            return;
                        }
                        Log.d(TAG, "Rewarded ad cached");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        rewardedCache.fail(loadToken);
                        Log.w(TAG, "Rewarded load failed: " + error.getMessage());
                    }
                }
            );
        } catch (RuntimeException error) {
            rewardedCache.fail(loadToken);
            Log.e(TAG, "Rewarded load threw", error);
        }
    }

    private void loadInterstitial() {
        long loadToken = interstitialCache.tryBeginLoad();
        if (loadToken == AdCache.REJECTED) {
            return;
        }
        try {
            InterstitialAd.load(
                new AdRequest.Builder(configuration.interstitialAdUnitId()).build(),
                new AdLoadCallback<>() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        if (!interstitialCache.complete(loadToken, ad) || disposed.get()
                            || !advertisingGate.canRequestAds()) {
                            ad.destroy();
                            interstitialCache.clear();
                            return;
                        }
                        Log.d(TAG, "Interstitial cached");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        interstitialCache.fail(loadToken);
                        Log.w(TAG, "Interstitial load failed: " + error.getMessage());
                    }
                }
            );
        } catch (RuntimeException error) {
            interstitialCache.fail(loadToken);
            Log.e(TAG, "Interstitial load threw", error);
        }
    }

    private void showRewardedOnMainThread(Activity activity, RewardedAd ad, long token,
                                          RewardedCallbackGate callbackGate) {
        if (!isUsable(activity) || disposed.get()) {
            ad.destroy();
            if (fullScreenGate.finish(token)) callbackGate.failed();
            loadAdsIfAllowed();
            return;
        }
        ad.setAdEventCallback(new RewardedAdEventCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                if (fullScreenGate.isCurrent(token)) callbackGate.opened();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                ad.destroy();
                if (fullScreenGate.finish(token)) callbackGate.dismissed();
                loadAdsIfAllowed();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(FullScreenContentError error) {
                Log.w(TAG, "Rewarded show failed: " + error.getMessage());
                ad.destroy();
                if (fullScreenGate.finish(token)) callbackGate.failed();
                loadAdsIfAllowed();
            }
        });
        try {
            ad.setImmersiveMode(true);
            ad.show(activity, ignored -> {
                if (!disposed.get()) callbackGate.rewardEarned();
            });
        } catch (RuntimeException error) {
            Log.e(TAG, "Rewarded show threw", error);
            ad.destroy();
            if (fullScreenGate.finish(token)) callbackGate.failed();
            loadAdsIfAllowed();
        }
    }

    private void showInterstitialOnMainThread(Activity activity, InterstitialAd ad, long token,
                                              AtomicBoolean opened, AtomicBoolean closed,
                                              FullScreenCallback callback) {
        if (!isUsable(activity) || disposed.get()) {
            ad.destroy();
            finishFullScreen(token, closed, callback, "Interstitial Activity lost");
            loadAdsIfAllowed();
            return;
        }
        ad.setAdEventCallback(new InterstitialAdEventCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                if (fullScreenGate.isCurrent(token) && opened.compareAndSet(false, true)) {
                    callback.onOpened();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                ad.destroy();
                finishFullScreen(token, closed, callback, null);
                loadAdsIfAllowed();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(FullScreenContentError error) {
                Log.w(TAG, "Interstitial show failed: " + error.getMessage());
                ad.destroy();
                finishFullScreen(token, closed, callback, null);
                loadAdsIfAllowed();
            }
        });
        try {
            ad.setImmersiveMode(true);
            ad.show(activity);
        } catch (RuntimeException error) {
            Log.e(TAG, "Interstitial show threw", error);
            ad.destroy();
            finishFullScreen(token, closed, callback, null);
            loadAdsIfAllowed();
        }
    }

    private void finishFullScreen(long token, AtomicBoolean closed,
                                  FullScreenCallback callback, String warning) {
        if (warning != null) Log.w(TAG, warning);
        if (fullScreenGate.finish(token) && closed.compareAndSet(false, true)
            && !disposed.get()) {
            callback.onClosed();
        }
    }

    private void clearCachedAds() {
        RewardedAd rewarded = rewardedCache.clear();
        InterstitialAd interstitial = interstitialCache.clear();
        runOnActivity(ignored -> {
            if (rewarded != null) rewarded.destroy();
            if (interstitial != null) interstitial.destroy();
        }, null);
    }

    private Activity currentActivity() {
        WeakReference<Activity> reference = activityReference;
        Activity activity = reference == null ? null : reference.get();
        return isUsable(activity) ? activity : null;
    }

    private boolean isUsable(Activity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void runOnActivity(ActivityAction action, Runnable unavailable) {
        Activity activity = currentActivity();
        if (activity == null || disposed.get()) {
            if (unavailable != null) unavailable.run();
            return;
        }
        activity.runOnUiThread(() -> {
            if (isUsable(activity) && !disposed.get()) {
                action.run(activity);
            } else if (unavailable != null) {
                unavailable.run();
            }
        });
    }

    @FunctionalInterface
    private interface ActivityAction {
        void run(Activity activity);
    }
}
