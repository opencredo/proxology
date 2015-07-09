package com.opencredo.proxology.utils;

public interface Unsafely<T> {
    static <T> T invoke(Unsafely<T> f) {
        try {
            return f.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    T run() throws Throwable;
}
