package com.opencredo.proxology.handlers.early;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface InterfaceInterpreter<S> {

    static <S> InterfaceInterpreter<S> caching(InterfaceInterpreter<S> uncachedInterpreter) {
        ConcurrentMap<Class<?>, UnboundMethodInterpreter<S>> cache = new ConcurrentHashMap<>();
        return iface -> cache.computeIfAbsent(iface, uncachedInterpreter::interpret);
    }

    UnboundMethodInterpreter<S> interpret(Class<?> iface);
}
