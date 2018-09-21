package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public interface Serializer {

    public static final Serializer DEFAULT = new Serializer() {
    };
    
    public static final ObjectMapper MAPPER = createObjectMapper();

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper() //
                .registerModule(new Jdk8Module()) //
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //
                .setSerializationInclusion(Include.NON_NULL);
    }

    default <T> T deserialize(String text, Class<? extends T> cls, ContextPath contextPath) {
        try {
            if (contextPath != null) {
                ObjectMapper m = createObjectMapper();
                Std iv = new InjectableValues.Std().addValue(ContextPath.class, contextPath);
                m.setInjectableValues(iv);
                return m.readValue(text, cls);
            } else {
                return MAPPER.readValue(text, cls);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default <T> T deserialize(String text, Class<T> cls) {
        return deserialize(text, cls, null);
    }

    default Optional<String> getODataType(String text) {
        try {
            ObjectNode node = new ObjectMapper().readValue(text, ObjectNode.class);
            return Optional.ofNullable(node.get("@odata.type")).map(JsonNode::asText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
