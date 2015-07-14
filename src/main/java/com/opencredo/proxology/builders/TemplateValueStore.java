package com.opencredo.proxology.builders;

import com.opencredo.proxology.handlers.MethodInterpreter;
import com.opencredo.proxology.proxies.Proxies;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.opencredo.proxology.handlers.InvocationHandlers.binding;
import static com.opencredo.proxology.handlers.InvocationHandlers.handlingDefaultMethods;

public final class TemplateValueStore<V, B extends Supplier<V>, T extends Template<V, B>> implements Supplier<V> {

    private final Class<T> templateClass;
    private final Map<String, Object> values = new HashMap<>();

    public TemplateValueStore(Class<T> templateClass) {
        this.templateClass = templateClass;
    }

    public static <V, B extends Supplier<V>, T extends Template<V, B>> B createBuilder(Class<T> templateClass) {
        TemplateValueStore<V, B, T> valueStore = new TemplateValueStore<>(templateClass);
        Class<B> builderClass = getBuilderClass(templateClass);
        return Proxies.simpleProxy(builderClass, valueStore.getMethodInterpreter(builderClass));
    }

    private static <B> Class<B> getBuilderClass(Class<?> templateClass) {
        Type secondTypeArgument = ((ParameterizedType) templateClass.getGenericInterfaces()[0]).getActualTypeArguments()[1];
        return (Class<B>) secondTypeArgument;
    }

    public MethodInterpreter getMethodInterpreter(Class<B> builderClass) {
        return handlingDefaultMethods(binding(
                this,
                BuilderClassInterpreter.interpret(builderClass).bind(values)));
    }

    public V get() {
        return Proxies.simpleProxy(
                templateClass,
                handlingDefaultMethods(
                        TemplateClassInterpreter.interpret(templateClass).bind(values)
                )
        ).get();
    }

}
