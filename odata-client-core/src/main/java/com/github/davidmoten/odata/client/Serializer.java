package com.github.davidmoten.odata.client;

import java.io.IOException;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Serializer {

    default <T> T deserialize(String text, Class<T> cls, ContextPath contextPath) {
        try {
            if (contextPath != null) {
                ObjectMapper m = Serialization.createObjectMapper();
                Std iv = new InjectableValues.Std().addValue(ContextPath.class, contextPath);
                return m.reader(iv).forType(cls).readValue(text);
            } else {
                return Serialization.MAPPER.readValue(text, cls);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default <T> T deserialize(String text, Class<T> cls) {
        return deserialize(text, cls, null);
    }

}
