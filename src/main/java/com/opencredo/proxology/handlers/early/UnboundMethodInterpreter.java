package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.handlers.MethodInterpreter;

import java.lang.reflect.Method;

@FunctionalInterface
public interface UnboundMethodInterpreter<S> {

    UnboundMethodCallHandler<S> interpret(Method method);

    default MethodInterpreter bind(S state) {
        return method -> interpret(method).bind(state);
    }
}
