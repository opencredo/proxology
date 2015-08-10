package com.opencredo.proxology.memoization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public final class Memoizer {

    private Memoizer() {
    }

    public static <I, O> Function<I, O> memoize(Function<I, O> f) {
        ConcurrentMap<I, O> cache = new ConcurrentHashMap<>();
        return i -> cache.computeIfAbsent(i, f);
    }

}
