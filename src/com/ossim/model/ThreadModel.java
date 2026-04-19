package com.ossim.model;

public enum ThreadModel {
    ONE_ONE("One-One"),
    MANY_ONE("Many-One"),
    MANY_MANY("Many-Many");

    private final String label;
    ThreadModel(String label) { this.label = label; }
    public String getLabel() { return label; }

    @Override public String toString() { return label; }
}
