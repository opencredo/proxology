package com.opencredo.proxology.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InvocationHandlers {

    private InvocationHandlers() {
    }

    public static InterpretingInvocationHandler caching(InterpretingInvocationHandler handler) {
        ConcurrentMap<Method, MethodCallHandler> cache = new ConcurrentHashMap<>();
        return method -> cache.computeIfAbsent(method, handler::interpret);
    }

    public static InterpretingInvocationHandler intercepting(InterpretingInvocationHandler handler,
                                                             MethodCallInterceptor interceptor) {
        return method -> interceptor.intercepting(method, handler.interpret(method));
    }

    public static InterpretingInvocationHandler handlingDefaultMethods(InterpretingInvocationHandler handler) {
        return method -> method.isDefault() ? DefaultMethodCallHandler.forMethod(method) : handler.interpret(method);
    }

    public static InterpretingInvocationHandler binding(Object target) {
        return binding(target, method -> {
            throw new IllegalStateException(String.format(
                    "Target class %s does not support method %s",
                    target.getClass(), method));
        });
    }

    public static InterpretingInvocationHandler binding(Object target, InterpretingInvocationHandler unboundHandler) {
        return method -> {
            if (method.getDeclaringClass().isAssignableFrom(target.getClass())) {
                MethodHandle handle = getMethodHandle(method, target);
                return (proxy, args) -> handle.invokeWithArguments(args);
            }
            return unboundHandler.interpret(method);
        };
    }

    private static MethodHandle getMethodHandle(Method method, Object target) {
        try {
            return MethodHandles.publicLookup().in(target.getClass()).unreflect(method).bindTo(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
