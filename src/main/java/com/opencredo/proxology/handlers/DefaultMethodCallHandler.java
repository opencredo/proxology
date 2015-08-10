package com.opencredo.proxology.handlers;

import com.opencredo.proxology.memoization.Memoizer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

final class DefaultMethodCallHandler {

    private DefaultMethodCallHandler() {
    }

    private static final Function<Method, MethodCallHandler> CACHE = Memoizer.memoize(m -> {
        MethodHandle handle = getMethodHandle(m);

        return (proxy, args) -> handle.bindTo(proxy).invokeWithArguments(args);
    });

    public static MethodCallHandler forMethod(Method method) {
        return CACHE.apply(method);
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
