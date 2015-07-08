package com.opencredo.proxology.proxies;

import com.opencredo.proxology.handlers.*;
import com.opencredo.proxology.handlers.early.EarlyBindingInterfaceInterpreter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.stream.Stream;

public final class Proxies {

    public static <T> T simpleProxy(Class<? extends T> iface, InvocationHandler handler, Class<?>...otherIfaces) {
        Class<?>[] allInterfaces = Stream.concat(
                Stream.of(iface),
                Stream.of(otherIfaces)
                        .filter(i -> !i.equals(iface)))
                .toArray(Class<?>[]::new);

        return (T) Proxy.newProxyInstance(iface.getClassLoader(),
                allInterfaces,
                handler);
    }

    public static <T> T cachedInterpretingProxy(Class<? extends T> iface, InterpretingInvocationHandler handler, Class<?>...otherIfaces) {
        return simpleProxy(iface, InvocationHandlers.caching(handler), otherIfaces);
    }

    public static <T> T intercepting(T target, Class<T> iface, MethodCallInterceptor interceptor) {
        return simpleProxy(iface,
                InvocationHandlers.intercepting(
                        InvocationHandlers.handlingDefaultMethods(
                                EarlyBindingInterfaceInterpreter.forClasses(iface, Object.class).bind(target)),
                        interceptor));
    }

    public static <T> T beanWrapping(Class<? extends T> iface, Map<String, Object> propertyValues) {
        PropertyValueStore store = new PropertyValueStore(iface, propertyValues);
        return simpleProxy(iface, store.createInvocationHandler(), Equalisable.class);
    }
}
