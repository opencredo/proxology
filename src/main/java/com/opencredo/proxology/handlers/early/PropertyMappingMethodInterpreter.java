package com.opencredo.proxology.handlers.early;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PropertyMappingMethodInterpreter {

    private PropertyMappingMethodInterpreter() {
    }

    private static final ConcurrentMap<Class<?>, UnboundMethodInterpreter<Map<String, Object>>> cache =
            new ConcurrentHashMap<>();

    public static UnboundMethodInterpreter<Map<String, Object>> forClass(Class<?> iface) {
        return cache.computeIfAbsent(iface, i -> UnboundMethodInterpreter.fromMethodMap(getMethodMap(i)));
    }

    private static Map<Method, UnboundMethodCallHandler<Map<String, Object>>> getMethodMap(Class<?> iface) {
        return Stream.of(iface.getMethods())
                .filter(PropertyMappingMethodInterpreter::isNonDefaultInstanceMethod)
                .collect(Collectors.toMap(
                        Function.identity(),
                        PropertyMappingMethodInterpreter::interpretMethod));
    }

    private static boolean isNonDefaultInstanceMethod(Method m) {
        return !m.isDefault() && !Modifier.isStatic(m.getModifiers());
    }

    private static UnboundMethodCallHandler<Map<String, Object>> interpretMethod(Method method) {
        String methodName = method.getName();

        if (methodName.startsWith("is") && method.getParameterCount() == 0) {
            return getterHandler(removePrefix(methodName, 2));
        }

        if (methodName.startsWith("get") && method.getParameterCount() == 0) {
            return getterHandler(removePrefix(methodName, 3));
        }

        if (methodName.startsWith("set") && method.getParameterCount() == 1) {
            return setterHandler(removePrefix(methodName, 3));
        }

        throw new IllegalArgumentException(String.format("Method %s is neither a getter nor a setter method", method));
    }

    private static String removePrefix(String name, int prefixLength) {
        return name.substring(prefixLength, prefixLength + 1).toLowerCase() + name.substring(prefixLength + 1);
    }

    private static UnboundMethodCallHandler<Map<String, Object>> getterHandler(String propertyName) {
        return propertyValues -> (proxy, args) -> propertyValues.get(propertyName);
    }

    private static UnboundMethodCallHandler<Map<String, Object>> setterHandler(String propertyName) {
        return propertyValues -> (proxy, args) -> {
            Object value = args[0];
            if (value == null) {
                propertyValues.remove(propertyName);
            } else {
                propertyValues.put(propertyName, value);
            }
            return null;
        };
    }

}
