package com.github.davidmoten.odata.client.generator;

class Indent {

    private String value = "";

    Indent left() {
        value = value.substring(0, value.length() - 4);
        return this;
    }

    Indent right() {
        value += "    ";
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

}
