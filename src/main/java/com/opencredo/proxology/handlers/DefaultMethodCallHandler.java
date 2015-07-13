package com.opencredo.proxology.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class DefaultMethodCallHandler {

    private DefaultMethodCallHandler() {
    }

    private static final ConcurrentMap<Method, MethodCallHandler> cache = new ConcurrentHashMap<>();

    public static MethodCallHandler forMethod(Method method) {
        return cache.computeIfAbsent(method, m -> {
            MethodHandle handle = getMethodHandle(m);

            return (proxy, args) -> handle.bindTo(proxy).invokeWithArguments(args);
        });
    }

    private static MethodHandle getMethodHandle(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
                    .unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
