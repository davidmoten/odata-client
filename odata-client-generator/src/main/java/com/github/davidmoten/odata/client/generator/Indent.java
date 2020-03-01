package com.github.davidmoten.odata.client.generator;

public final class Indent {

    public static final String INDENT = "    ";

    private String value;

    public Indent() {
        this("");
    }

    public Indent(String value) {
        this.value = value;
    }

    public Indent left() {
        if (value.length() < INDENT.length()) {
            throw new RuntimeException("left called more than right");
        }
        value = value.substring(0, value.length() - INDENT.length());
        return this;

    }

    public Indent right() {
        value += INDENT;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    public Indent copy() {
        return new Indent(value);
    }

}
