package com.opencredo.proxology.handlers.early;

import java.lang.reflect.Method;
import java.util.Map;

public final class PropertyMappingClassInterpreter implements MethodMappingClassInterpreter<Map<String, Object>> {

    private static final ClassInterpreter<Map<String, Object>> cached = ClassInterpreter.cached(
            new PropertyMappingClassInterpreter());

    public static UnboundMethodInterpreter<Map<String, Object>> interpret(Class<?> iface) {
        return cached.getMethodInterpreter(iface);
    }

    private PropertyMappingClassInterpreter() {
    }

    @Override
    public UnboundMethodCallHandler<Map<String, Object>> interpretMethod(Method method) {
        String methodName = method.getName();

        if (hasGetterSignature(method)) {
            if (methodName.startsWith("is")) {
                return getterHandler(removePrefix(methodName, 2));
            }

            if (methodName.startsWith("get")) {
                return getterHandler(removePrefix(methodName, 3));
            }
        }

        if (hasSetterSignature(method) && methodName.startsWith("set")) {
            return setterHandler(removePrefix(methodName, 3));
        }

        throw new IllegalArgumentException(String.format("Method %s is neither a getter nor a setter method", method));
    }

    private boolean hasGetterSignature(Method method) {
        return method.getParameterCount() == 0 && !method.getReturnType().equals(void.class);
    }

    private boolean hasSetterSignature(Method method) {
        return method.getParameterCount() == 1 && method.getReturnType().equals(void.class);
    }

    private String removePrefix(String name, int prefixLength) {
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
