package com.github.davidmoten.odata.client;

public interface QueryOption {

    public static CollectionRequestOptionsBuilder requestHeader(String key, String value) {
        return new CollectionRequestOptionsBuilder().requestHeader(key, value);
    }

}
