package com.opencredo.proxology.arguments;

import com.opencredo.proxology.reflection.TypeInfo;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentConversion {

    public static Object convert(TypeInfo wantedType, Object argument) {
        Class<?> argumentClass = argument.getClass();

        if (wantedType.isPrimitive() || wantedType.getRawType().isAssignableFrom(argumentClass)) {
            return argument;
        }

        if (argumentClass.isArray()) {
            return convertStream(wantedType, Stream.of(toObjectArray(argument)));
        }

        if (argument instanceof Collection) {
            return convertStream(wantedType, ((Collection<Object>) argument).stream());
        }

        if (argument instanceof Supplier) {
            return convert(wantedType, ((Supplier<Object>) argument).get());
        }

        throw new IllegalArgumentException("Cannot convert " + argument.getClass() + " to " + wantedType);
    }

    private static Object[] toObjectArray(Object argument) {
        if (argument instanceof Object[]) {
            return (Object[]) argument;
        }
        Object[] result = new Object[Array.getLength(argument)];
        for (int i = 0; i < result.length; i++) {
            result[i] = Array.get(argument, i);
        }
        return result;
    }

    private static Object convertStream(TypeInfo wantedType, Stream<Object> stream) {
        if (List.class.isAssignableFrom(wantedType.getRawType())) {
            TypeInfo itemType = wantedType.getInterface(List.class).getFirstTypeArgument();
            return stream.map(o -> convert(itemType, o)).collect(Collectors.toList());
        }

        if (wantedType.getRawType().equals(Set.class)) {
            TypeInfo itemType = wantedType.getInterface(Set.class).getFirstTypeArgument();
            return stream.map(o -> convert(itemType, o)).collect(Collectors.toSet());
        }

        if (wantedType.isArray()) {
            return stream.map(o -> convert(wantedType.getArrayComponentType(), o)).toArray(Object[]::new);
        }

        throw new IllegalArgumentException("Cannot convert stream to " + wantedType);
    }
}
