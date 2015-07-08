package com.opencredo.proxology.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@FunctionalInterface
public interface InterpretingInvocationHandler extends InvocationHandler {

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodCallHandler handler = interpret(method);
        return handler.invoke(proxy, args);
    }

    MethodCallHandler interpret(Method method);

    default InterpretingInvocationHandler orElse(InterpretingInvocationHandler next) {
        return method -> {
            MethodCallHandler handler = interpret(method);
            return handler == null
                    ? next.interpret(method)
                    : handler;
        };
    }
}
