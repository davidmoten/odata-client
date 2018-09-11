package com.github.davidmoten.odata.client;

public interface QueryOption {

    public static QueryOptionsBuilder requestHeader(String key, String value) {
        return new QueryOptionsBuilder().requestHeader(key, value);
    }

}
