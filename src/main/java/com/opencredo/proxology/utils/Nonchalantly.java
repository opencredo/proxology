package com.opencredo.proxology.utils;

public interface Nonchalantly<T> {
    static <T> T invoke(Nonchalantly<T> f) {
        try {
            return f.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    T run() throws Throwable;
}
