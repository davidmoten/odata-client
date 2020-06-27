package com.github.davidmoten.odata.client;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class JacksonTest {

    private static final JacksonAnnotationIntrospector ANNOTATION_INSPECTOR = new JacksonAnnotationIntrospector();
    private static final ObjectMapper MAPPER = createObjectMapper();

    static ObjectMapper createObjectMapper() {
        return new ObjectMapper() //
                .setAnnotationIntrospector(ANNOTATION_INSPECTOR);
    }

    // THIS ONE FAILS on 2.11.0
    @Test
    public void testNoError1() throws JsonProcessingException {
        MAPPER.writeValueAsString(new Thing());
    }

    // THIS ONE PASSES on 2.11.0
    @Test
    public void testNoError2() throws JsonProcessingException {
        createObjectMapper().writeValueAsString(new Thing());
    }

    public static final class Thing {
        @JsonProperty
        String name = "Bert";
    }

}
