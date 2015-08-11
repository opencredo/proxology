package com.opencredo.proxology.utils;

import org.junit.Test;

public class NonchalantlyTest {

    @Test(expected = OutOfMemoryError.class)
    public void passesOnError() {
        Nonchalantly.invoke(() -> { throw new OutOfMemoryError(); });
    }
}
