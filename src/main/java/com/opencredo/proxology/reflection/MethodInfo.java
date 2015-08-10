package com.opencredo.proxology.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodInfo {

    private enum Prefix {
        GET,
        SET,
        WITH,
        IS;

        public static Prefix forMethodName(String methodName) {
            return Stream.of(Prefix.values())
                    .filter(p -> methodName.startsWith(p.name().toLowerCase()))
                    .findAny()
                    .orElse(null);
        }

        public String getPropertyName(String methodName) {
            int prefixLength = name().length();
            if (methodName.length() <= prefixLength) {
                throw new IllegalArgumentException("Method name " + methodName + " shorted than prefix " + name());
            }
            return methodName.substring(prefixLength, prefixLength + 1).toLowerCase()
                    + methodName.substring(prefixLength + 1);
        }

        public boolean isGetterPrefix() {
            return GET.equals(this) || IS.equals(this);
        }

        public boolean isSetterPrefix() {
            return SET.equals(this);
        }

        public boolean isBuilderPrefix() {
            return WITH.equals(this);
        }
    }

    public static MethodInfo forMethod(Method method) {
        return forMethod(method, TypeInfo.forType(method.getDeclaringClass()));
    }

    public static MethodInfo forMethod(Method method, TypeInfo declaringType) {
        String methodName = method.getName();

        return new MethodInfo(method, methodName, declaringType, Prefix.forMethodName(methodName));
    }


    public MethodInfo(Method method, String methodName, TypeInfo declaringType, Prefix prefix) {
        this.method = method;
        this.methodName = methodName;
        this.declaringType = declaringType;
        this.prefix = prefix;
    }

    private final Method method;
    private final String methodName;
    private final TypeInfo declaringType;
    private final Prefix prefix;

    public boolean isGetter() {
        return prefix.isGetterPrefix()
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class);
    }

    public boolean isSetter() {
        return prefix.isSetterPrefix()
                && method.getParameterCount() == 1
                && method.getReturnType().equals(void.class);
    }

    public String getPropertyName() {
        return prefix == null ? methodName : prefix.getPropertyName(methodName);
    }

    public List<TypeInfo> getPropertyTypes() {
        return Stream.of(method.getGenericParameterTypes()).map(TypeInfo::forType).collect(Collectors.toList());
    }

    public TypeInfo getReturnType() {
        return TypeInfo.forType(method.getGenericReturnType());
    }

    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    public boolean isDefault() {
        return method.isDefault();
    }

    public Method getMethod() {
        return method;
    }

    public TypeInfo getDeclaringType() {
        return declaringType;
    }
}
