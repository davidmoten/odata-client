package com.github.davidmoten.odata.client;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EntityPreconditions {

    // TODO why not used in generated classes?
    public static void checkIsAscii(String v) {
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(v)) {
            throw new RuntimeException("illegal encoding, must be ascii");
        }
    }

    public static void checkIsAscii(Optional<String> v) {
        if (v.isPresent()) {
            checkIsAscii(v.get());
        }
    }

}
