package com.opencredo.proxology.handlers;

import com.opencredo.proxology.handlers.early.BeanMappingInterfaceInterpreter;
import com.opencredo.proxology.handlers.early.EarlyBindingInterfaceInterpreter;

import java.util.Map;

public class PropertyValueStore implements Equalisable {

    private final Class<?> forClass;
    private final Map<String, Object> propertyValues;

    public PropertyValueStore(Class<?> forClass, Map<String, Object> propertyValues) {
        this.forClass = forClass;
        this.propertyValues = propertyValues;
    }

    @Override
    public int hashCode() {
        return propertyValues.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s %s", forClass, propertyValues);
    }

    @Override
    public boolean equals(Object o) {
        return isEqualTo(forClass, o);
    }

    public InterpretingInvocationHandler createInvocationHandler() {
        return EarlyBindingInterfaceInterpreter.forClasses(Object.class, Equalisable.class).bind(this)
            .orElse(InvocationHandlers.handlingDefaultMethods(
                BeanMappingInterfaceInterpreter.getCached().interpret(forClass).bind(propertyValues)));
    }

    @Override
    public Object getState() {
        return propertyValues;
    }
}
