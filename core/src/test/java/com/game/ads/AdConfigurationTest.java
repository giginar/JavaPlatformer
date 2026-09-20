package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdConfigurationTest {
    @Test
    void disabledModeContainsNoIdentifiersAndRequestsNothing() {
        AdConfiguration configuration = AdConfiguration.disabled();

        assertEquals(AdMode.DISABLED, configuration.mode());
        assertFalse(configuration.adsEnabled());
        assertEquals("", configuration.appId());
        assertEquals("", configuration.rewardedAdUnitId());
        assertEquals("", configuration.interstitialAdUnitId());
    }

    @Test
    void testModeAlwaysUsesOfficialGoogleDemoIdentifiers() {
        AdConfiguration configuration = AdConfiguration.test();

        assertEquals(AdMode.TEST, configuration.mode());
        assertTrue(configuration.adsEnabled());
        assertEquals(AdConfiguration.TEST_APP_ID, configuration.appId());
        assertEquals(AdConfiguration.TEST_REWARDED_AD_UNIT_ID,
            configuration.rewardedAdUnitId());
        assertEquals(AdConfiguration.TEST_INTERSTITIAL_AD_UNIT_ID,
            configuration.interstitialAdUnitId());
    }

    @Test
    void productionRequiresAllIdentifiers() {
        assertThrows(IllegalArgumentException.class,
            () -> AdConfiguration.production("", "", ""));
    }

    @Test
    void productionRejectsEveryDemoIdentifier() {
        String app = "ca-app-pub-1234567890123456~1234567890";
        String rewarded = "ca-app-pub-1234567890123456/1234567890";
        String interstitial = "ca-app-pub-1234567890123456/0987654321";

        assertThrows(IllegalArgumentException.class, () -> AdConfiguration.production(
            AdConfiguration.TEST_APP_ID, rewarded, interstitial));
        assertThrows(IllegalArgumentException.class, () -> AdConfiguration.production(
            app, AdConfiguration.TEST_REWARDED_AD_UNIT_ID, interstitial));
        assertThrows(IllegalArgumentException.class, () -> AdConfiguration.production(
            app, rewarded, AdConfiguration.TEST_INTERSTITIAL_AD_UNIT_ID));
    }

    @Test
    void productionAcceptsExternallySuppliedNonDemoIdentifiers() {
        AdConfiguration configuration = AdConfiguration.production(
            "ca-app-pub-1234567890123456~1234567890",
            "ca-app-pub-1234567890123456/1234567890",
            "ca-app-pub-1234567890123456/0987654321");

        assertEquals(AdMode.PRODUCTION, configuration.mode());
        assertTrue(configuration.adsEnabled());
    }
}
