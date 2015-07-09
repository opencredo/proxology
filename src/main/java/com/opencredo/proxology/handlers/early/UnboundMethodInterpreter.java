package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.handlers.MethodInterpreter;

import java.lang.reflect.Method;
import java.util.Map;

@FunctionalInterface
public interface UnboundMethodInterpreter<S> {

    static <S> UnboundMethodInterpreter<S> fromMethodMap(Map<Method, UnboundMethodCallHandler<S>> methodMap) {
        return methodMap::get;
    }

    UnboundMethodCallHandler<S> interpret(Method method);

    default MethodInterpreter bind(S state) {
        return method -> interpret(method).bind(state);
    }
}
