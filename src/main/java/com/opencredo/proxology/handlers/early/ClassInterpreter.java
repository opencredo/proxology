package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.methods.MethodInfo;
import com.opencredo.proxology.reflection.TypeInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionalInterface
public interface ClassInterpreter<T> {

    static <T> ClassInterpreter<T> cached(ClassInterpreter<T> interpreter) {
        ConcurrentMap<Class<?>, UnboundMethodInterpreter<T>> cache = new ConcurrentHashMap<>();
        return iface -> cache.computeIfAbsent(iface, interpreter::interpret);
    }

    static <T> ClassInterpreter<T> mappingWith(UnboundMethodInterpreter<T> interpreter) {
        return iface -> TypeInfo.forType(iface).streamDeclaredMethods()
                .filter(m -> !m.isDefault() && !m.isStatic())
                .map(MethodInfo::getMethod)
                .collect(Collectors.toMap(
                        Function.identity(),
                        interpreter::interpret))
                ::get;
    }

    UnboundMethodInterpreter<T> interpret(Class<?> iface);

}
