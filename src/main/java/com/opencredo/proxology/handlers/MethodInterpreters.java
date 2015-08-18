package com.opencredo.proxology.handlers;

import com.opencredo.proxology.memoization.Memoizer;
import com.opencredo.proxology.utils.EqualisableByState;
import com.opencredo.proxology.utils.Nonchalantly;

import java.lang.reflect.Method;

public final class MethodInterpreters {

    private static final Method EQUALS_METHOD = Nonchalantly.invoke(() -> Object.class.getMethod("equals", Object.class));

    private MethodInterpreters() {
    }

    public static MethodInterpreter caching(MethodInterpreter interpreter) {
        return Memoizer.memoize(interpreter::interpret)::apply;
    }

    public static MethodInterpreter binding(Object target) {
        return binding(target, method -> {
            throw new IllegalStateException(String.format(
                    "Target class %s does not support method %s",
                    target.getClass(), method));
        });
    }

    public static MethodInterpreter binding(Object target, MethodInterpreter unboundInterpreter) {
        MethodCallHandler equaliser = getEqualiserFor(target);

        return method -> {
            if (method.equals(EQUALS_METHOD)) {
                return equaliser;
            }

            if (method.getDeclaringClass().isAssignableFrom(target.getClass())) {
                return (proxy, args) -> method.invoke(target, args);
            }

            return unboundInterpreter.interpret(method);
        };
    }

    private static MethodCallHandler getEqualiserFor(Object target) {
        if (target instanceof EqualisableByState) {
            Object targetState = ((EqualisableByState) target).getState();
            return (proxy, args) -> hasEqualState(targetState, args[0]);
        }

        return (proxy, args) -> target.equals(args[0]);
    }

    private static boolean hasEqualState(Object state, Object other) {
        return other instanceof EqualisableByState
                && state.equals(((EqualisableByState) other).getState());
    }

    public static MethodInterpreter intercepting(MethodInterpreter interpreter,
                                                 MethodCallInterceptor interceptor) {
        return method -> interceptor.intercepting(method, interpreter.interpret(method));
    }

    public static MethodInterpreter handlingDefaultMethods(MethodInterpreter nonDefaultInterpreter) {
        return method -> method.isDefault()
                ? DefaultMethodCallHandler.forMethod(method)
                : nonDefaultInterpreter.interpret(method);
    }

}
