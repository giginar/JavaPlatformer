package com.game.diver.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.game.DeepDiveDrift;

public final class AndroidLauncher extends AndroidApplication {
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
        initialize(new DeepDiveDrift(), configuration);
    }
}
