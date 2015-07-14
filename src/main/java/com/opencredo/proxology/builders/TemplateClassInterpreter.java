package com.opencredo.proxology.builders;

import com.opencredo.proxology.handlers.early.ClassInterpreter;
import com.opencredo.proxology.handlers.early.UnboundMethodCallHandler;
import com.opencredo.proxology.handlers.early.UnboundMethodInterpreter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public final class TemplateClassInterpreter {
    private static final ClassInterpreter<Map<String, Object>> cache =
            ClassInterpreter.cached(
                    ClassInterpreter.mappingWith(TemplateClassInterpreter::interpret));

    public static UnboundMethodInterpreter<Map<String, Object>> interpret(Class<?> templateClass) {
        return cache.interpret(templateClass);
    }

    private static UnboundMethodCallHandler<Map<String, Object>> interpret(Method method) {
        String methodName = method.getName();
        String propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        return state -> (proxy, args) -> state.getOrDefault(propertyName, interpret(args[0]));
    }

    private static Object interpret(Object arg) {
        if (arg.getClass().isArray()) {
            return Arrays.asList(((Object[]) arg));
        }
        return arg;
    }

}
