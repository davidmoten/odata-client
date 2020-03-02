package com.github.davidmoten.odata.client;

import static com.github.davidmoten.odata.client.InlineParameterSyntax.encode;
import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.davidmoten.odata.client.internal.TypedObject;

public class InlineParameterSyntaxTest {

    @Test
    public void testString() {
        Map<String, TypedObject> parameters = new HashMap<>();
        parameters.put("name", new TypedObject("Edm.String", "fred"));
        assertEquals("(name=\"fred\")", encode(Serializer.INSTANCE, parameters));
    }

    @Test
    public void testBoolean() {
        Map<String, TypedObject> parameters = new HashMap<>();
        parameters.put("name", new TypedObject("Edm.Boolean", false));
        assertEquals("(name=false)", encode(Serializer.INSTANCE, parameters));
    }

    @Test
    public void testInteger() {
        Map<String, TypedObject> parameters = new HashMap<>();
        parameters.put("name", new TypedObject("Edm.Int32", 123));
        assertEquals("(name=123)", encode(Serializer.INSTANCE, parameters));
    }

    @Test
    public void testDate() {
        OffsetDateTime t = OffsetDateTime.parse("2007-12-03T10:15:30+01:00");
        Map<String, TypedObject> parameters = new HashMap<>();
        parameters.put("name", new TypedObject("Edm.DateTimeOffset", t));
        assertEquals("(name=\"2007-12-03T10:15:30+01:00\")",
                encode(Serializer.INSTANCE, parameters));
    }

}
