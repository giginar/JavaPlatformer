package com.game.diver.android;

import android.os.Bundle;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.game.DeepDiveDrift;
import com.game.ads.AdConfiguration;
import com.game.ads.AdMode;
import com.game.ads.AdvertisingService;
import com.game.diver.android.generated.AdBuildConfiguration;

public final class AndroidLauncher extends AndroidApplication {
    private static final long IMMERSIVE_RETRY_DELAY_MILLIS = 250L;
    private static final String TAG = "DeepDriftQa";
    private static final String QA_REWARDED_EXTRA = "deepdive.qa.rewarded";
    private static final int QA_REWARDED_MAX_ATTEMPTS = 60;

    private AndroidAdvertisingService advertising;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.useGyroscope = false;
        configuration.useRotationVectorSensor = false;
        configuration.useImmersiveMode = true;
        configuration.useWakelock = true;
        configuration.numSamples = 4;
        AdConfiguration adConfiguration = AdBuildConfiguration.create();
        advertising = new AndroidAdvertisingService(this, adConfiguration);
        DeepDiveDrift game = new DeepDiveDrift(advertising);
        initialize(game, configuration);
        advertising.start();
        startRewardedQaIfRequested(game, adConfiguration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (advertising != null) {
            advertising.onActivityResumed(this);
        }
        restoreImmersiveMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            restoreImmersiveMode();
        }
    }

    @Override
    protected void onDestroy() {
        if (advertising != null) {
            advertising.detachActivity(this);
            advertising.dispose();
        }
        super.onDestroy();
    }

    private void restoreImmersiveMode() {
        if (advertising != null && advertising.isFullScreenContentActive()) {
            return;
        }
        applyImmersiveMode();
        getWindow().getDecorView().postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()
                && (advertising == null || !advertising.isFullScreenContentActive())) {
                applyImmersiveMode();
            }
        }, IMMERSIVE_RETRY_DELAY_MILLIS);
    }

    private void applyImmersiveMode() {
        useImmersiveMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars()
                    | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    private void startRewardedQaIfRequested(DeepDiveDrift game,
                                            AdConfiguration configuration) {
        if (configuration.mode() != AdMode.TEST
            || !getIntent().getBooleanExtra(QA_REWARDED_EXTRA, false)) {
            return;
        }
        pollRewardedQa(game, 0);
    }

    private void pollRewardedQa(DeepDiveDrift game, int attempt) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (!advertising.isRewardedAvailable()) {
            if (attempt >= QA_REWARDED_MAX_ATTEMPTS) {
                Log.w(TAG, "Rewarded TEST QA timed out before an ad became available");
                return;
            }
            getWindow().getDecorView().postDelayed(
                () -> pollRewardedQa(game, attempt + 1), 500L);
            return;
        }
        boolean accepted = game.showRewarded(new AdvertisingService.RewardedCallback() {
            @Override
            public void onOpened() {
                Log.i(TAG, "Rewarded TEST QA opened");
            }

            @Override
            public void onRewardEarned() {
                Log.i(TAG, "Rewarded TEST QA callback: earned (no gameplay benefit granted)");
            }

            @Override
            public void onClosed() {
                Log.i(TAG, "Rewarded TEST QA closed");
            }
        });
        Log.i(TAG, "Rewarded TEST QA show accepted: " + accepted);
    }
}
