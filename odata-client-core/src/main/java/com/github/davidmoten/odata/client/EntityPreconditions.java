package com.github.davidmoten.odata.client;

import java.nio.charset.StandardCharsets;

public class EntityPreconditions {

    public static void checkIsAscii(String v) {
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(v)) {
            throw new RuntimeException("illegal encoding, must be ascii");
        }
    }

}
