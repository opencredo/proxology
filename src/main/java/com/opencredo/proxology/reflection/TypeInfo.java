package com.opencredo.proxology.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeInfo {

    public static TypeInfo forType(Type type) {
        return new TypeInfo(type);
    }

    private final Type type;

    private TypeInfo(Type type) {
        this.type = type;
    }

    public boolean isParameterized() {
        return type instanceof ParameterizedType;
    }

    public ParameterizedType getParameterizedType() {
        return (ParameterizedType) type;
    }

    public List<TypeInfo> getTypeArguments() {
        return Stream.of(getParameterizedType().getActualTypeArguments())
                .map(TypeInfo::forType)
                .collect(Collectors.toList());
    }

    public <T> Class<T> getRawType() {
        return (Class<T>) (isParameterized()
                ? getParameterizedType().getRawType()
                : type);
    }

    public TypeInfo getFirstTypeArgument() {
        return getTypeArguments().get(0);
    }

    public TypeInfo getSecondTypeArgument() {
        return getTypeArguments().get(1);
    }

    public List<TypeInfo> getInterfaces() {
        return streamInterfaces().collect(Collectors.toList());
    }

    public TypeInfo getInterface(Class<?> iface) {
        if (iface.equals(getRawType())) {
            return this;
        }
        return streamInterfaces().filter(t -> t.getRawType().equals(iface)).findAny()
                .orElseThrow(() ->
                        new IllegalArgumentException("Type " + type + " does not have interface " + iface));
    }

    private Stream<TypeInfo> streamInterfaces() {
        return Stream.of(getRawType().getGenericInterfaces())
                .map(TypeInfo::forType);
    }

    public boolean isArray() {
        return getRawType().isArray();
    }

    public Stream<MethodInfo> streamDeclaredMethods() {
        return Stream.of(getRawType().getDeclaredMethods()).map(method -> MethodInfo.forMethod(method, this));
    }

    public TypeInfo getArrayComponentType() {
        return TypeInfo.forType(getRawType().getComponentType());
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public boolean isPrimitive() {
        return getRawType().isPrimitive();
    }
}
