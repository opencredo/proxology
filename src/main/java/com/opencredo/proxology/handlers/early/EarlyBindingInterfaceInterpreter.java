package com.opencredo.proxology.handlers.early;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EarlyBindingInterfaceInterpreter {

    private static final ConcurrentMap<Set<Class<?>>, UnboundMethodInterpreter<Object>> cachedInterfaces =
            new ConcurrentHashMap<>();

    public static UnboundMethodInterpreter<Object> forClasses(Class<?>...classes) {
        Set<Class<?>> classSet = Stream.of(classes).collect(Collectors.toSet());
        return cachedInterfaces.computeIfAbsent(classSet, EarlyBindingInterfaceInterpreter::forClassesUncached);
    }
    private static UnboundMethodInterpreter<Object> forClassesUncached(Set<Class<?>> classes) {
        return UnboundMethodInterpreter.fromMethodMap(classes.stream()
                .map(EarlyBindingInterfaceInterpreter::getMethodMap)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static Map<Method,UnboundMethodCallHandler<Object>> getMethodMap(Class<?> iface) {
        return Stream.of(iface.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !m.isDefault() && !Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        EarlyBindingInterfaceInterpreter::getHandler
                ));
    }

    private static UnboundMethodCallHandler<Object> getHandler(Method method) {
        MethodHandle handle = getMethodHandle(method);

        return target -> (proxy, args) -> handle.bindTo(target).invokeWithArguments(args);
    }

    private static MethodHandle getMethodHandle(Method method) {
        try {
            return MethodHandles.publicLookup().in(method.getDeclaringClass()).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
