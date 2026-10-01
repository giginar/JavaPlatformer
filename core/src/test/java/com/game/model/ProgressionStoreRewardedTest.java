package com.game.model;

import com.game.ads.AdvertisingService;
import com.game.ads.RewardedCallbackGate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionStoreRewardedTest {
    private static final long DAY = 86_400_000L;

    @Test
    void dailySalvageGrantsExactlyFiveOncePerUtcDayAndSurvivesRestart() {
        MemoryPreferences preferences = new MemoryPreferences();
        AtomicLong now = new AtomicLong(Instant.parse("2026-10-01T12:00:00Z").toEpochMilli());
        ProgressionStore store = new ProgressionStore(preferences, now::get);

        assertTrue(store.isDailySalvageEligible());
        assertTrue(store.claimDailySalvage());
        assertEquals(5, store.pearls());
        assertFalse(store.claimDailySalvage());
        assertEquals(5, store.pearls());

        ProgressionStore reopened = new ProgressionStore(preferences, now::get);
        assertFalse(reopened.isDailySalvageEligible());
        assertFalse(reopened.claimDailySalvage());
        assertEquals(5, reopened.pearls());

        now.addAndGet(DAY);
        assertTrue(reopened.claimDailySalvage());
        assertEquals(10, reopened.pearls());
    }

    @Test
    void clockRollbackNeverReopensTheGreatestClaimedUtcDay() {
        MemoryPreferences preferences = new MemoryPreferences();
        AtomicLong now = new AtomicLong(Instant.parse("2026-10-05T12:00:00Z").toEpochMilli());
        ProgressionStore store = new ProgressionStore(preferences, now::get);
        assertTrue(store.claimDailySalvage());

        now.addAndGet(-3L * DAY);

        assertFalse(new ProgressionStore(preferences, now::get).isDailySalvageEligible());
        assertFalse(store.claimDailySalvage());
        assertEquals(5, store.pearls());
    }

    @Test
    void dismissalOrFailureCannotMutateWalletWithoutRewardClaim() {
        MemoryPreferences preferences = new MemoryPreferences();
        ProgressionStore store = new ProgressionStore(preferences,
            () -> Instant.parse("2026-10-01T12:00:00Z").toEpochMilli());

        AdvertisingService.RewardedCallback callback = new AdvertisingService.RewardedCallback() {
            @Override public void onOpened() {
            }

            @Override public void onRewardEarned() {
                store.claimDailySalvage();
            }

            @Override public void onClosed() {
            }

            @Override public void onFailedToShow() {
            }
        };

        RewardedCallbackGate failed = new RewardedCallbackGate(callback);
        failed.failed();
        assertEquals(0, store.pearls());
        assertTrue(store.isDailySalvageEligible());

        RewardedCallbackGate dismissed = new RewardedCallbackGate(callback);
        dismissed.opened();
        dismissed.dismissed();
        assertEquals(0, store.pearls());

        RewardedCallbackGate earned = new RewardedCallbackGate(callback);
        earned.opened();
        earned.rewardEarned();
        earned.rewardEarned();
        assertEquals(5, store.pearls());
    }

    @Test
    void resultsBonusUsesActualOrdinaryAwardAndRoundsOnce() {
        MemoryPreferences preferences = new MemoryPreferences();
        ProgressionStore store = new ProgressionStore(preferences);
        long first = store.beginRun();
        assertEquals(25, store.awardRun(first, 2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(5, store.resultsBonus(first));
        assertEquals(5, store.claimResultsBonus(first));
        assertEquals(30, store.pearls());

        long second = store.beginRun();
        assertEquals(83, store.awardRun(second, 8_399f, 1f, EquipmentLoadout.NONE));
        assertEquals(17, store.resultsBonus(second));
        assertEquals(17, store.claimResultsBonus(second));
        assertEquals(130, store.pearls());

        assertEquals(0, ProgressionStore.calculateResultsBonus(2));
        assertEquals(1, ProgressionStore.calculateResultsBonus(3));
        assertEquals(1, ProgressionStore.calculateResultsBonus(7));
        assertEquals(2, ProgressionStore.calculateResultsBonus(8));
    }

    @Test
    void resultsBonusRequiresCompletedCurrentRunAndRejectsStaleOrDuplicateClaims() {
        MemoryPreferences preferences = new MemoryPreferences();
        ProgressionStore store = new ProgressionStore(preferences);
        long stale = store.beginRun();

        assertFalse(store.isResultsBonusEligible(stale));
        assertEquals(0, store.claimResultsBonus(stale));

        long current = store.beginRun();
        assertEquals(25, store.awardRun(current, 2_599f, 1f, EquipmentLoadout.NONE));
        assertEquals(0, store.claimResultsBonus(stale));
        assertEquals(5, store.claimResultsBonus(current));
        assertEquals(0, store.claimResultsBonus(current));
        assertEquals(30, store.pearls());
    }

    @Test
    void recreationCannotDuplicateResultsBonusAndNewRunInvalidatesCallback() {
        MemoryPreferences preferences = new MemoryPreferences();
        ProgressionStore store = new ProgressionStore(preferences);
        long completed = store.beginRun();
        assertEquals(25, store.awardRun(completed, 2_599f, 1f, EquipmentLoadout.NONE));

        ProgressionStore recreated = new ProgressionStore(preferences);
        assertEquals(5, recreated.resultsBonus(completed));
        assertEquals(5, recreated.claimResultsBonus(completed));
        assertEquals(0, new ProgressionStore(preferences).claimResultsBonus(completed));

        long newer = recreated.beginRun();
        assertTrue(newer > completed);
        assertEquals(0, recreated.claimResultsBonus(completed));
        assertEquals(30, recreated.pearls());
    }
}
