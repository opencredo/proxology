package com.opencredo.proxology.utils;

import java.util.function.Supplier;

@FunctionalInterface
public interface FallibleSupplier<T> {

    static <T> FallibleSupplier<T> of(Supplier<T> supplier) {
        return supplier::get;
    }

    static <T> FallibleSupplier<T> constant(T constant) {
        return () -> constant;
    }

    static <T> FallibleSupplier<T> constantFailure(Throwable  t) {
        return () -> { throw t; };
    }

    T get() throws Throwable;
}
