package com.opencredo.proxology.handlers.early;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EarlyBindingInterfaceInterpreter implements InterfaceInterpreter<Object> {

    private static final InterfaceInterpreter<Object> cached = InterfaceInterpreter.caching(
            new EarlyBindingInterfaceInterpreter());

    public static InterfaceInterpreter<Object> cached() {
        return cached;
    }

    public static UnboundMethodInterpreter<Object> forClasses(Class<?>...classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("No classes supplied");
        }
        UnboundMethodInterpreter<Object> result = cached.interpret(classes[0]);
        for (int i = 1; i < classes.length; i++) {
            result = result.orElse(cached.interpret(classes[i]));
        }
        return result;
    }

    @Override
    public UnboundMethodInterpreter<Object> interpret(Class<?> iface) {
        return UnboundMethodInterpreter.fromMethodMap(getMethodMap(iface));
    }

    private Map<Method,UnboundMethodCallHandler<Object>> getMethodMap(Class<?> iface) {
        return Stream.of(iface.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !m.isDefault() && !Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getHandler
                ));
    }

    private UnboundMethodCallHandler<Object> getHandler(Method method) {
        MethodHandle handle = getMethodHandle(method);

        return target -> (proxy, args) -> handle.bindTo(target).invokeWithArguments(args);
    }

    private MethodHandle getMethodHandle(Method method) {
        try {
            return MethodHandles.publicLookup().in(method.getDeclaringClass()).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
