package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializerTest {

    @Test
    public void testDeserializeODataValue() {
        OffsetDateTime t = OffsetDateTime.parse("2007-12-03T10:15:30+01:00");
        String text = Util
                .utf8(SerializerTest.class.getResourceAsStream("/odata-value-offsetdatetime.json"));
        ContextPath cp = new ContextPath(null, null);
        @SuppressWarnings("unchecked")
        ODataValue<OffsetDateTime> o = Serializer.INSTANCE.deserializeWithParametricType( //
                text, //
                ODataValue.class, //
                OffsetDateTime.class, //
                cp, //
                false);
        assertEquals(t.toInstant().toEpochMilli(), o.value().toInstant().toEpochMilli());
    }

    @Test
    public void testOverrideJsonIncludeAnnotation() throws JsonProcessingException {
        ObjectMapper m = Serializer.createObjectMapper(true);
        String json = m.writeValueAsString(new Thing());
        assertEquals("{\"name\":\"Bert\",\"address\":null}", json);
        Thing t = m.readValue(json, Thing.class);
        assertEquals("Bert", t.name);
    }
    
    @Test
    public void testDontOverrideJsonIncludeAnnotation() throws JsonProcessingException {
        ObjectMapper m = Serializer.createObjectMapper(false);
        String json = m.writeValueAsString(new Thing());
        assertEquals("{\"name\":\"Bert\"}", json);
    }
    
    @Test
    public void testSerializerSerializeDoesNotIncludeNull() throws JsonProcessingException {
        String json = Serializer.INSTANCE.serialize(new Thing());
        assertEquals("{\"name\":\"Bert\"}", json);
    }
    
    @JsonInclude(Include.NON_NULL)
    static final class Thing {
        @JsonProperty
        String name = "Bert";
        
        @JsonProperty
        String address = null;
    }

}