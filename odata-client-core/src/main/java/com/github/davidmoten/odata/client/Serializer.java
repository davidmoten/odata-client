package com.github.davidmoten.odata.client;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface Serializer {

    default <T> T deserialize(String json, Class<T> cls) {
        ObjectMapper m = Serialization.MAPPER;
        try {
            return m.readValue(json, cls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
