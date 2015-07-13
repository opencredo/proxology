package com.opencredo.proxology;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class PassthroughInvocationHandler implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static <T> T proxying(T target, Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[] { iface },
                new PassthroughInvocationHandler(target));
    }

    private final Object target;

    public PassthroughInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
