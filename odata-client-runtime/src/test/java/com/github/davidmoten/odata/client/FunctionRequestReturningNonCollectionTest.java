package com.github.davidmoten.odata.client;

import static com.github.davidmoten.odata.client.FunctionRequestReturningNonCollection.toInlineParameterSyntax;
import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class FunctionRequestReturningNonCollectionTest {

    @Test
    public void testString() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "fred");
        assertEquals("(name=\"fred\")", toInlineParameterSyntax(Serializer.INSTANCE, parameters));
    }
    
    @Test
    public void testBoolean() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", false);
        assertEquals("(name=false)", toInlineParameterSyntax(Serializer.INSTANCE, parameters));
    }
    
    @Test
    public void testInteger() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", 123);
        assertEquals("(name=123)", toInlineParameterSyntax(Serializer.INSTANCE, parameters));
    }
    
    @Test
    public void testDate() {
        OffsetDateTime t = OffsetDateTime.parse("2007-12-03T10:15:30+01:00");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", t);
        assertEquals("(name=\"2007-12-03T10:15:30+01:00\")", toInlineParameterSyntax(Serializer.INSTANCE, parameters));
    }
    
}
