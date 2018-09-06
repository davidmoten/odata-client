package com.github.davidmoten.odata.client;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class Serialization {

    private Serialization() {
        // prevent instantiation
    }

    public static final ObjectMapper MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper() //
                .registerModule(new Jdk8Module()) //
                .setSerializationInclusion(Include.NON_NULL);
    }

}
