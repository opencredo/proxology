package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.methods.MethodInfo;

import java.lang.reflect.Method;
import java.util.Map;

public final class PropertyMappingClassInterpreter {

    private static final ClassInterpreter<Map<String, Object>> cached = ClassInterpreter.cached(
            ClassInterpreter.mappingWith(PropertyMappingClassInterpreter::interpret));

    public static UnboundMethodInterpreter<Map<String, Object>> interpret(Class<?> iface) {
        return cached.interpret(iface);
    }

    private PropertyMappingClassInterpreter() {
    }

    private static UnboundMethodCallHandler<Map<String, Object>> interpret(Method method) {
        MethodInfo info = MethodInfo.forMethod(method);

        if (info.isGetter()) {
            return getterHandler(info.getPropertyName());
        }
        if (info.isSetter()) {
            return setterHandler(info.getPropertyName());
        }

        throw new IllegalArgumentException(String.format("Method %s is neither a getter nor a setter method", method));
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
