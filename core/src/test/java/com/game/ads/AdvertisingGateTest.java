package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvertisingGateTest {
    @Test
    void consentMustAllowRequestsBeforeInitializationOrLoading() {
        AdvertisingGate gate = new AdvertisingGate(true);

        assertFalse(gate.tryBeginInitialization());
        gate.updateConsent(false, true);
        assertFalse(gate.tryBeginInitialization());
        assertFalse(gate.canLoadAds());
        assertTrue(gate.isPrivacyOptionsRequired());
    }

    @Test
    void initializationCanBeginOnlyOnceAndLoadingWaitsForCompletion() {
        AdvertisingGate gate = new AdvertisingGate(true);
        gate.updateConsent(true, false);

        assertTrue(gate.tryBeginInitialization());
        assertFalse(gate.tryBeginInitialization());
        assertFalse(gate.canLoadAds());
        gate.markInitialized();
        assertTrue(gate.canLoadAds());
    }

    @Test
    void revokedConsentStopsNewLoadsAfterInitialization() {
        AdvertisingGate gate = new AdvertisingGate(true);
        gate.updateConsent(true, false);
        assertTrue(gate.tryBeginInitialization());
        gate.markInitialized();
        assertTrue(gate.canLoadAds());

        gate.updateConsent(false, true);
        assertFalse(gate.canLoadAds());
        assertTrue(gate.isPrivacyOptionsRequired());
    }

    @Test
    void failedInitializationCanRetryWithoutAllowingDuplicateActiveAttempts() {
        AdvertisingGate gate = new AdvertisingGate(true);
        gate.updateConsent(true, false);

        assertTrue(gate.tryBeginInitialization());
        assertFalse(gate.tryBeginInitialization());
        gate.markInitializationFailed();
        assertTrue(gate.tryBeginInitialization());
    }

    @Test
    void disabledModeIgnoresConsentAndPrivacyState() {
        AdvertisingGate gate = new AdvertisingGate(false);
        gate.updateConsent(true, true);

        assertFalse(gate.canRequestAds());
        assertFalse(gate.isPrivacyOptionsRequired());
        assertFalse(gate.tryBeginInitialization());
    }
}
