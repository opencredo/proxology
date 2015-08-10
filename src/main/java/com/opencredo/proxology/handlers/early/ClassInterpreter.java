package com.opencredo.proxology.handlers.early;

import com.opencredo.proxology.memoization.Memoizer;
import com.opencredo.proxology.methods.MethodInfo;
import com.opencredo.proxology.reflection.TypeInfo;

import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionalInterface
public interface ClassInterpreter<T> {

    static <T> ClassInterpreter<T> cached(ClassInterpreter<T> interpreter) {
        return Memoizer.memoize(interpreter::interpret)::apply;
    }

    static <T> ClassInterpreter<T> mappingWith(UnboundMethodInterpreter<T> interpreter) {
        return iface -> TypeInfo.forType(iface).streamDeclaredMethods()
                    .filter(m -> !m.isDefault() && !m.isStatic())
                    .map(MethodInfo::getMethod)
                    .collect(Collectors.toMap(
                            Function.identity(),
                            interpreter::interpret
                    ))::get;
    }

    UnboundMethodInterpreter<T> interpret(Class<?> iface);

}
