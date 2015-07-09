package com.opencredo.proxology.handlers;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InvocationHandlers {

    private InvocationHandlers() {
    }

    public static MethodInterpreter caching(MethodInterpreter handler) {
        ConcurrentMap<Method, MethodCallHandler> cache = new ConcurrentHashMap<>();
        return method -> cache.computeIfAbsent(method, handler::interpret);
    }

    public static MethodInterpreter intercepting(MethodInterpreter handler,
                                                             MethodCallInterceptor interceptor) {
        return method -> {
            MethodCallHandler methodCallHandler = handler.interpret(method);
            return methodCallHandler == null ? null : interceptor.intercepting(method, methodCallHandler);
        };
    }

    public static MethodInterpreter handlingDefaultMethods(MethodInterpreter handler) {
        return method -> method.isDefault() ? DefaultMethodCallHandler.forMethod(method) : handler.interpret(method);
    }

}
