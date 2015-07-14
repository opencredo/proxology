package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.handlers.MethodInterpreter;

import java.lang.reflect.Method;
import java.util.function.Predicate;

@FunctionalInterface
public interface UnboundMethodInterpreter<S> {

    static <S> UnboundMethodInterpreter<S> matching(
            Predicate<Method> selector,
            UnboundMethodInterpreter<S> matchedInterpreter,
            UnboundMethodInterpreter<S> unmatchedInterpreter) {
        return method -> selector.test(method)
                ? matchedInterpreter.interpret(method)
                : unmatchedInterpreter.interpret(method);
    }

    UnboundMethodCallHandler<S> interpret(Method method);

    default MethodInterpreter bind(S state) {
        return method -> interpret(method).bind(state);
    }
}
