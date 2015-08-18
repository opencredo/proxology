package com.opencredo.proxology.utils;

import org.junit.Test;

import java.io.IOException;

public class NonchalantlyTest {

    @Test(expected = OutOfMemoryError.class)
    public void passesOnError() {
        Nonchalantly.invoke(() -> { throw new OutOfMemoryError(); });
    }

    @Test(expected = IllegalStateException.class)
    public void doesNotAttemptNonsensicalConversion() throws IOException {
        Nonchalantly.<Void, IOException>invoke(() -> {
            throw new IllegalStateException();
        });
    }

}
