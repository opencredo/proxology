package com.opencredo.proxology.handlers;

import com.opencredo.proxology.handlers.early.PropertyMappingMethodInterpreter;
import com.opencredo.proxology.handlers.early.UnboundDispatchingMethodInterpreter;

import java.util.Map;

public class PropertyValueStore implements Equalisable {

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

    public InterpretingInvocationHandler createInvocationHandler() {
        return UnboundDispatchingMethodInterpreter.forClasses(Object.class, Equalisable.class).bind(this)
            .orElse(InvocationHandlers.handlingDefaultMethods(
                    PropertyMappingMethodInterpreter.forClass(iface).bind(propertyValues)));
    }

    @Override
    public Object getState() {
        return propertyValues;
    }
}
