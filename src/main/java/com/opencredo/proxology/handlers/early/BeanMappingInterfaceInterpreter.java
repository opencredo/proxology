package com.opencredo.proxology.handlers.early;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanMappingInterfaceInterpreter implements InterfaceInterpreter<Map<String, Object>> {

    private static final InterfaceInterpreter<Map<String, Object>> cached = InterfaceInterpreter.caching(new BeanMappingInterfaceInterpreter());

    public static InterfaceInterpreter<Map<String, Object>> getCached() {
        return cached;
    }

    @Override
    public UnboundMethodInterpreter<Map<String, Object>> interpret(Class<?> iface) {
        return UnboundMethodInterpreter.fromMethodMap(getMethodMap(iface));
    }

    private Map<Method, UnboundMethodCallHandler<Map<String, Object>>> getMethodMap(Class<?> iface) {
        return Stream.of(iface.getDeclaredMethods())
                .filter(m -> !m.isDefault() && !Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::interpretMethod));
    }

    private UnboundMethodCallHandler<Map<String, Object>> interpretMethod(Method method) {
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

    private UnboundMethodCallHandler<Map<String, Object>> getterHandler(String propertyName) {
        return propertyValues -> (proxy, args) -> propertyValues.get(propertyName);
    }

    private UnboundMethodCallHandler<Map<String, Object>> setterHandler(String propertyName) {
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
