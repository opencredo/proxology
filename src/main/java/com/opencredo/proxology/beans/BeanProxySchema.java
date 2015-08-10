package com.opencredo.proxology.beans;

import com.opencredo.proxology.handlers.MethodInterpreter;
import com.opencredo.proxology.handlers.early.ClassInterpreter;
import com.opencredo.proxology.handlers.early.UnboundMethodInterpreter;
import com.opencredo.proxology.memoization.Memoizer;
import com.opencredo.proxology.methods.MethodInfo;
import com.opencredo.proxology.utils.Nonchalantly;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BeanProxySchema {

    private static final Function<Class<?>, BeanProxySchema> CACHED = Memoizer.memoize(BeanProxySchema::forClassUncached);

    public static BeanProxySchema forClass(Class<?> iface) {
        return CACHED.apply(iface);
    }

    private static BeanProxySchema forClassUncached(Class<?> iface) {
        BeanInfo beanInfo = Nonchalantly.invoke(() -> Introspector.getBeanInfo(iface));
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

        String[] propertyNames = Stream.of(descriptors)
                .map(PropertyDescriptor::getName)
                .toArray(String[]::new);

        return new BeanProxySchema(
                propertyNames,
                ClassInterpreter.mappingWith(getInterpreter(propertyNames)).interpret(iface));
    }

    private static UnboundMethodInterpreter<BeanProxyStorage> getInterpreter(String[] propertyNames) {
        return method -> {
            MethodInfo methodInfo = MethodInfo.forMethod(method);
            int storageSlotIndex = Arrays.binarySearch(propertyNames, methodInfo.getPropertyName());

            if (methodInfo.isGetter()) {
                return storage -> (proxy, args) -> storage.get(storageSlotIndex);
            }

            if (methodInfo.isSetter()) {
                return storage -> (proxy, args) -> storage.set(storageSlotIndex, args[0]);
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
