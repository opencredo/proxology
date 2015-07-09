package com.opencredo.proxology.proxies;

import com.opencredo.proxology.handlers.Equalisable;
import com.opencredo.proxology.handlers.InvocationHandlers;
import com.opencredo.proxology.handlers.MethodCallInterceptor;
import com.opencredo.proxology.handlers.PropertyValueStore;
import com.opencredo.proxology.handlers.early.UnboundDispatchingMethodInterpreter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Proxies {

    @SuppressWarnings("unchecked")
    public static <T> T simpleProxy(Class<? extends T> iface, InvocationHandler handler, Class<?>...otherIfaces) {
        Set<Class<?>> allInterfaces = new HashSet<>();
        allInterfaces.add(iface);
        allInterfaces.addAll(Arrays.asList(otherIfaces));

        return (T) Proxy.newProxyInstance(iface.getClassLoader(),
                allInterfaces.stream().toArray(Class<?>[]::new),
                handler);
    }

    public static <T> T intercepting(T target, Class<T> iface, MethodCallInterceptor interceptor) {
        return simpleProxy(iface,
                InvocationHandlers.intercepting(
                        InvocationHandlers.handlingDefaultMethods(
                                UnboundDispatchingMethodInterpreter.forClasses(iface, Object.class).bind(target)),
                        interceptor));
    }

    public static <T> T beanWrapping(Class<? extends T> iface, Map<String, Object> propertyValues) {
        PropertyValueStore store = new PropertyValueStore(iface, propertyValues);
        return simpleProxy(iface, store.createInvocationHandler(), Equalisable.class);
    }
}
