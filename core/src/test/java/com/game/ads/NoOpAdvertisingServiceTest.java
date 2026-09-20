package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NoOpAdvertisingServiceTest {
    @Test
    void desktopNoOpNeverExposesOrShowsAdvertising() {
        NoOpAdvertisingService service = new NoOpAdvertisingService();
        AdvertisingService.FullScreenCallback callback = new AdvertisingService.FullScreenCallback() {
            @Override
            public void onOpened() {
                throw new AssertionError("No-op callback must not open");
            }

            @Override
            public void onClosed() {
                throw new AssertionError("No-op callback must not close");
            }
        };

        assertFalse(service.canRequestAds());
        assertFalse(service.isPrivacyOptionsRequired());
        assertFalse(service.isRewardedAvailable());
        assertFalse(service.isInterstitialAvailable());
        assertFalse(service.isFullScreenContentActive());
        assertFalse(service.showPrivacyOptions(callback));
        assertFalse(service.showInterstitial(callback));
        service.dispose();
    }
}
