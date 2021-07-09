package com.github.davidmoten.odata.client.edm;

import com.fasterxml.jackson.annotation.JsonValue;

public final class UnsignedByte {
    
    @JsonValue
    private int value;

    public UnsignedByte(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
