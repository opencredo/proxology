package com.opencredo.proxology.handlers.early;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@FunctionalInterface
public interface ClassInterpreter<T> {

    static <T> ClassInterpreter<T> cached(ClassInterpreter<T> interpreter) {
        ConcurrentMap<Class<?>, UnboundMethodInterpreter<T>> cache = new ConcurrentHashMap<>();
        return iface -> cache.computeIfAbsent(iface, interpreter::getMethodInterpreter);
    }

    UnboundMethodInterpreter<T> getMethodInterpreter(Class<?> iface);

}
