package com.github.davidmoten.odata.client.generator;

class Indent {

    private String value = "";
    public static final String INDENT = "    ";

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

}
