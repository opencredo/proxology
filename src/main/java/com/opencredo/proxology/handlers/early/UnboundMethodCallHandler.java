package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.handlers.MethodCallHandler;

@FunctionalInterface
public interface UnboundMethodCallHandler<S> {
    MethodCallHandler bind(S state);
}
