package com.opencredo.proxology.beans;

import com.opencredo.proxology.handlers.MethodInterpreter;
import com.opencredo.proxology.handlers.early.ClassInterpreter;
import com.opencredo.proxology.handlers.early.UnboundMethodInterpreter;
import com.opencredo.proxology.memoization.Memoizer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BeanProxySchema {

    private static final Function<Class<?>, BeanProxySchema> CACHED = Memoizer.memoize(BeanProxySchema::forClassUncached);

    public static BeanProxySchema forClass(Class<?> iface) {
        return CACHED.apply(iface);
    }

    private static BeanProxySchema forClassUncached(Class<?> iface) {
        BeanPropertyAnalysis ifacePropertyAnalysis = BeanPropertyAnalysis.forClass(iface);

        return new BeanProxySchema(
                ifacePropertyAnalysis.getPropertyNames(),
                ClassInterpreter.mappingWith(
                        getInterpreter(
                                ifacePropertyAnalysis.getGetterIndices(),
                                ifacePropertyAnalysis.getSetterIndices()))
                        .interpret(iface));
    }

    private static UnboundMethodInterpreter<BeanProxyStorage> getInterpreter(Map<Method, Integer> getterIndices, Map<Method, Integer> setterIndices) {
        return method -> {
            if (getterIndices.containsKey(method)) {
                int slotIndex = getterIndices.get(method);
                return storage -> (proxy, args) -> storage.get(slotIndex);
            }

            if (setterIndices.containsKey(method)) {
                int slotIndex = setterIndices.get(method);
                return storage -> (proxy, args) -> storage.set(slotIndex, args[0]);
            }

            throw new IllegalArgumentException(String.format("Method %s is neither a getter nor a setter", method));
        };
    }

    private final String[] propertyNames;
    private final UnboundMethodInterpreter<BeanProxyStorage> unboundMethodInterpreter;

    public BeanProxySchema(String[] propertyNames, UnboundMethodInterpreter<BeanProxyStorage> unboundMethodInterpreter) {
        this.propertyNames = propertyNames;
        this.unboundMethodInterpreter = unboundMethodInterpreter;
    }

    public String formatValues(Object[] data) {
        return IntStream.range(0, propertyNames.length)
                .mapToObj(i -> String.format("%s: %s", propertyNames[i], data[i]))
                .collect(Collectors.joining(",", "{", "}"));
    }

    public BeanProxyStorage createStorage() {
        return new BeanProxyStorage(this, new Object[propertyNames.length]);
    }

    public MethodInterpreter getMethodInterpreter(BeanProxyStorage storage) {
        return unboundMethodInterpreter.bind(storage);
    }
}
