package com.opencredo.proxology.builders;

import java.util.function.Supplier;

public interface Template<T, B extends Supplier<T>> extends Supplier<T> {
    static <V, B extends Supplier<V>, T extends Template<V, B>> B builderFor(Class<T> templateClass) {
        return TemplateValueStore.createBuilder(templateClass);
    }
}
