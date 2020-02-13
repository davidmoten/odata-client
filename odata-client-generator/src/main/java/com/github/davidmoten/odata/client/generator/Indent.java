package com.github.davidmoten.odata.client.generator;

final class Indent {

    public static final String INDENT = "    ";

    private String value;

    Indent() {
        this("");
    }

    Indent(String value) {
        this.value = value;
    }

    Indent left() {
        if (value.length() < INDENT.length()) {
            throw new RuntimeException("left called more than right");
        }
        value = value.substring(0, value.length() - INDENT.length());
        return this;

    }

    Indent right() {
        value += INDENT;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    Indent copy() {
        return new Indent(value);
    }

}
