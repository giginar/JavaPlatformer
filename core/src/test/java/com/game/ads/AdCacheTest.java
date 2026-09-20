package com.game.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdCacheTest {
    @Test
    void maintainsOneLoadingOrCachedObject() {
        AdCache<String> cache = new AdCache<>();
        long load = cache.tryBeginLoad();

        assertTrue(load != AdCache.REJECTED);
        assertTrue(cache.isLoading());
        assertEquals(AdCache.REJECTED, cache.tryBeginLoad());
        assertTrue(cache.complete(load, "rewarded"));
        assertTrue(cache.isAvailable());
        assertEquals("rewarded", cache.take());
        assertFalse(cache.isAvailable());
        assertTrue(cache.tryBeginLoad() != AdCache.REJECTED);
    }

    @Test
    void rejectsStaleLoadCallbacksAfterClear() {
        AdCache<String> cache = new AdCache<>();
        long staleLoad = cache.tryBeginLoad();

        assertNull(cache.clear());
        assertFalse(cache.complete(staleLoad, "stale"));
        assertFalse(cache.isAvailable());
    }

    @Test
    void failureClearsOnlyTheMatchingLoad() {
        AdCache<String> cache = new AdCache<>();
        long load = cache.tryBeginLoad();

        assertFalse(cache.fail(load + 1));
        assertTrue(cache.isLoading());
        assertTrue(cache.fail(load));
        assertFalse(cache.isLoading());
    }
}
