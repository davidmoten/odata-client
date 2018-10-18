package com.github.davidmoten.odata.client;

public class Util {

    public static <T> T nvl(T object, T ifNull) {
        if (object == null) {
            return ifNull;
        } else {
            return object;
        }
    }
}
