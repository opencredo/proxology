package com.opencredo.proxology.handlers.early;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface MethodMappingClassInterpreter<T> extends ClassInterpreter<T> {

    @Override
    default UnboundMethodInterpreter<T> getMethodInterpreter(Class<?> iface) {
        return UnboundMethodInterpreter.fromMethodMap(getMethodMap(iface));
    }

    default Map<Method, UnboundMethodCallHandler<T>> getMethodMap(Class<?> iface) {
        return Stream.of(iface.getMethods())
                .filter(m -> !m.isDefault() && !Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::interpretMethod));
    }

    UnboundMethodCallHandler<T> interpretMethod(Method method);
}
