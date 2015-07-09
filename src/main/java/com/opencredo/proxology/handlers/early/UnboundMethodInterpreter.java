package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.handlers.InterpretingInvocationHandler;

import java.lang.reflect.Method;
import java.util.Map;

@FunctionalInterface
public interface UnboundMethodInterpreter<S> {

    static <S> UnboundMethodInterpreter<S> fromMethodMap(Map<Method, UnboundMethodCallHandler<S>> methodMap) {
        return methodMap::get;
    }

    UnboundMethodCallHandler<S> interpret(Method method);

    default InterpretingInvocationHandler bind(S state) {
        return method -> {
            UnboundMethodCallHandler<S> handler = interpret(method);
            return handler == null ? null : handler.bind(state);
        };
    }
}
