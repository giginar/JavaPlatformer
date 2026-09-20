package com.game.diver.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.game.DeepDiveDrift;
import com.game.ads.AdConfiguration;
import com.game.diver.android.generated.AdBuildConfiguration;

public final class AndroidLauncher extends AndroidApplication {
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
        initialize(new DeepDiveDrift(advertising), configuration);
        advertising.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (advertising != null) {
            advertising.onActivityResumed(this);
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
}
