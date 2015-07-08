package com.opencredo.proxology.handlers;

import java.lang.reflect.Method;
import java.util.Map;

public class BeanMappingInvocationHandler implements InterpretingInvocationHandler, Equalisable {

    public static BeanMappingInvocationHandler mapping(Class<?> mappedClass, Map<String, Object> properties) {
        return new BeanMappingInvocationHandler(mappedClass, properties);
    }

    private final Class<?> mappedClass;
    private final Map<String, Object> propertyValues;

    private BeanMappingInvocationHandler(Class<?> mappedClass, Map<String, Object> propertyValues) {
        this.mappedClass = mappedClass;
        this.propertyValues = propertyValues;
    }

    @Override
    public MethodCallHandler interpret(Method method) {
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

    private MethodCallHandler getterHandler(String propertyName) {
        return (proxy, args) -> propertyValues.get(propertyName);
    }

    private MethodCallHandler setterHandler(String propertyName) {
        return (proxy, args) -> {
            Object value = args[0];
            if (value == null) {
                propertyValues.remove(propertyName);
            } else {
                propertyValues.put(propertyName, value);
            }
            return null;
        };
    }

    @Override
    public int hashCode() {
        return propertyValues.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return isEqualTo(mappedClass, other);
    }

    @Override
    public String toString() {
        return String.format("%s %s", mappedClass.getSimpleName(), propertyValues);
    }

    @Override
    public Object getState() {
        return propertyValues;
    }
}
