package com.game.ads;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardedCallbackGateTest {
    @Test
    void deliversOpenedRewardAndCloseExactlyOnce() {
        Counters counters = new Counters();
        RewardedCallbackGate gate = new RewardedCallbackGate(counters);

        gate.opened();
        gate.opened();
        gate.rewardEarned();
        gate.rewardEarned();
        gate.dismissed();
        gate.dismissed();

        assertEquals(1, counters.opened.get());
        assertEquals(1, counters.rewarded.get());
        assertEquals(1, counters.closed.get());
    }

    @Test
    void dismissalWithoutRewardDoesNotInventOne() {
        Counters counters = new Counters();
        RewardedCallbackGate gate = new RewardedCallbackGate(counters);

        gate.opened();
        gate.dismissed();

        assertEquals(0, counters.rewarded.get());
        assertEquals(1, counters.closed.get());
    }

    @Test
    void showFailureClosesOnceAndRejectsLateReward() {
        Counters counters = new Counters();
        RewardedCallbackGate gate = new RewardedCallbackGate(counters);

        gate.failed();
        gate.rewardEarned();
        gate.failed();

        assertEquals(0, counters.rewarded.get());
        assertEquals(1, counters.closed.get());
    }

    private static final class Counters implements AdvertisingService.RewardedCallback {
        private final AtomicInteger opened = new AtomicInteger();
        private final AtomicInteger rewarded = new AtomicInteger();
        private final AtomicInteger closed = new AtomicInteger();

        @Override
        public void onOpened() {
            opened.incrementAndGet();
        }

        @Override
        public void onRewardEarned() {
            rewarded.incrementAndGet();
        }

        @Override
        public void onClosed() {
            closed.incrementAndGet();
        }
    }
}
