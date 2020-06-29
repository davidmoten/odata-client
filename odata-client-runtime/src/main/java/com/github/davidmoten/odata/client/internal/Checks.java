package com.github.davidmoten.odata.client.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class Checks {
    
    private Checks() {
        // prevent instantiation
    }

    public static String checkIsAscii(String v) {
        if (v != null && !StandardCharsets.US_ASCII.newEncoder().canEncode(v)) {
            throw new IllegalArgumentException("illegal encoding, must be ascii: " + v);
        } else {
            return v;
        }
    }
    
    public static List<String> checkIsAscii(List<String> list) {
        list.forEach(v -> checkIsAscii(v));
        return list;
    }

}
