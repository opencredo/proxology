package com.opencredo.proxology.handlers;

public interface Equalisable {

    Object getState();

    default boolean isEqualTo(Class<?> expectedClass, Object other) {
        if (!(other instanceof Equalisable && expectedClass.isAssignableFrom(other.getClass()))) {
            return false;
        }
        return getState().equals(((Equalisable) other).getState());
    }
}
