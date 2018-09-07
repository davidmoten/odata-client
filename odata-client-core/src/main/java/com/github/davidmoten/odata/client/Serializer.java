package com.github.davidmoten.odata.client;

public interface Serializer {

    <T> T deserialize(String json, Class<T> cls);
    
}
