package com.opencredo.proxology.handlers;

import com.opencredo.proxology.handlers.early.PropertyMappingClassInterpreter;

import java.util.Map;
import java.util.function.Supplier;

import static com.opencredo.proxology.handlers.InvocationHandlers.binding;
import static com.opencredo.proxology.handlers.InvocationHandlers.handlingDefaultMethods;

public class PropertyValueStore implements Equalisable, Supplier<Map<String, Object>> {

    private final Class<?> iface;
    private final Map<String, Object> propertyValues;

    public PropertyValueStore(Class<?> iface, Map<String, Object> propertyValues) {
        this.iface = iface;
        this.propertyValues = propertyValues;
    }

    @Override
    public int hashCode() {
        return propertyValues.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s %s", iface, propertyValues);
    }

    @Override
    public boolean equals(Object o) {
        return isEqualTo(iface, o);
    }

    public MethodInterpreter createInvocationHandler() {
        return binding(this,
                handlingDefaultMethods(
                        PropertyMappingClassInterpreter.interpret(iface).bind(propertyValues)));
    }

    @Override
    public Object getState() {
        return propertyValues;
    }

    @Override
    public Map<String, Object> get() {
        return propertyValues;
    }
}
