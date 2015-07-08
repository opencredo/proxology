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

    public static MethodCallHandler forMethod(Method method) {
        MethodHandle handle = getMethodHandle(method);

        return (proxy, args) -> handle.bindTo(proxy).invokeWithArguments(args);
    }

    private static MethodHandle getMethodHandle(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();

        try {
            return privateLookupFor(declaringClass).unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ConcurrentMap<Class<?>, MethodHandles.Lookup> privateLookups = new ConcurrentHashMap<>();

    private static MethodHandles.Lookup privateLookupFor(Class<?> lookupClass) {
        return privateLookups.computeIfAbsent(lookupClass, DefaultMethodCallHandler::uncachedPrivateLookupFor);
    }

    private static MethodHandles.Lookup uncachedPrivateLookupFor(Class<?> lookupClass) {
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
        return constructor.newInstance(lookupClass, MethodHandles.Lookup.PRIVATE);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
