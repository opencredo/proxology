package com.opencredo.proxology.builders;

import com.opencredo.proxology.arguments.ArgumentConversion;
import com.opencredo.proxology.handlers.early.ClassInterpreter;
import com.opencredo.proxology.handlers.early.UnboundMethodCallHandler;
import com.opencredo.proxology.handlers.early.UnboundMethodInterpreter;
import com.opencredo.proxology.methods.MethodInfo;

import java.lang.reflect.Method;
import java.util.Map;

public final class TemplateClassInterpreter {
    private static final ClassInterpreter<Map<String, Object>> cache =
            ClassInterpreter.cached(
                    ClassInterpreter.mappingWith(TemplateClassInterpreter::interpret));

    public static UnboundMethodInterpreter<Map<String, Object>> interpret(Class<?> templateClass) {
        return cache.interpret(templateClass);
    }

    private static UnboundMethodCallHandler<Map<String, Object>> interpret(Method method) {
        MethodInfo methodInfo = MethodInfo.forMethod(method);
        String propertyName = methodInfo.getPropertyName();
        return state -> (proxy, args) -> ArgumentConversion.convert(
                methodInfo.getReturnType(),
                state.getOrDefault(propertyName, args[0]));
    }

}
