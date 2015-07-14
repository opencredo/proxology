package com.opencredo.proxology.handlers.early;

import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface ClassInterpreter<T> {

    static <T> ClassInterpreter<T> cached(ClassInterpreter<T> interpreter) {
        ConcurrentMap<Class<?>, UnboundMethodInterpreter<T>> cache = new ConcurrentHashMap<>();
        return iface -> cache.computeIfAbsent(iface, interpreter::interpret);
    }

    static <T> ClassInterpreter<T> mappingWith(UnboundMethodInterpreter<T> interpreter) {
        return iface -> Stream.of(iface.getDeclaredMethods())
                .filter(m -> !m.isDefault() && !Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        interpreter::interpret))
                ::get;
    }

    UnboundMethodInterpreter<T> interpret(Class<?> iface);

}
