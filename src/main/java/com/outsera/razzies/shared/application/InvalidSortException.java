package com.outsera.razzies.shared.application;

/** A {@link PageQuery} asks to sort by a property the resource does not have. */
public class InvalidSortException extends RuntimeException {

    private final String property;

    public InvalidSortException(String property) {
        super("Invalid sort property '%s'".formatted(property));
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
