package com.game.ads;

/** One-object ad cache with generation tokens that reject stale asynchronous loads. */
public final class AdCache<T> {
    public static final long REJECTED = 0L;

    private long nextLoadToken = 1L;
    private long activeLoadToken = REJECTED;
    private T loaded;

    public synchronized long tryBeginLoad() {
        if (activeLoadToken != REJECTED || loaded != null) {
            return REJECTED;
        }
        activeLoadToken = nextLoadToken++;
        if (nextLoadToken == REJECTED) {
            nextLoadToken++;
        }
        return activeLoadToken;
    }

    public synchronized boolean complete(long token, T ad) {
        if (token == REJECTED || token != activeLoadToken || ad == null) {
            return false;
        }
        activeLoadToken = REJECTED;
        loaded = ad;
        return true;
    }

    public synchronized boolean fail(long token) {
        if (token == REJECTED || token != activeLoadToken) {
            return false;
        }
        activeLoadToken = REJECTED;
        return true;
    }

    public synchronized T take() {
        T result = loaded;
        loaded = null;
        return result;
    }

    public synchronized T clear() {
        activeLoadToken = REJECTED;
        T result = loaded;
        loaded = null;
        return result;
    }

    public synchronized boolean isAvailable() {
        return loaded != null;
    }

    public synchronized boolean isLoading() {
        return activeLoadToken != REJECTED;
    }
}
