package com.github.davidmoten.odata.client;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

//TODO rename because used by Action/Function parameters not just entity fields
public final class EntityPreconditions {
    
    private EntityPreconditions() {
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

    public static Optional<String> checkIsAscii(Optional<String> v) {
        if (v.isPresent()) {
            checkIsAscii(v.get());
        }
        return v;
    }

}
