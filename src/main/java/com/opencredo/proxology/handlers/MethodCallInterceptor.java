package com.opencredo.proxology.handlers;

import java.lang.reflect.Method;

@FunctionalInterface
public interface MethodCallInterceptor {

    Object intercept(Object proxy, Method method, Object[] args, MethodCallHandler handler) throws Throwable;

    default MethodCallHandler intercepting(Method method, MethodCallHandler handler) {
        return (proxy, args) -> intercept(proxy, method, args, handler);
    }
}
