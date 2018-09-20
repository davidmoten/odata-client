package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CollectionPageJsonTest {

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        String json = "{\"@odata.nextLink\": \"abc123\",\n" //
                + "\"value\": [ {\"name\": \"fred\", \"surname\":\"dag\"}]}";
        ObjectMapper m = new ObjectMapper();
        CollectionPageJson c = m.readValue(json, CollectionPageJson.class);
        assertEquals("abc123", c.nextLink().get());
        assertEquals("fred", c.values().get(0).get("name").asText());
    }
}