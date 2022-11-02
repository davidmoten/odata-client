package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.odata.client.Serializer;

import test3a.enums.DayOfWeek;

public class Test3ServiceTest {
    
    @Test
    public void testEnumDefaultValue() {
        assertEquals("\"futureValue\"", Serializer.INSTANCE.serialize(DayOfWeek.FUTURE_VALUE));
        assertEquals(DayOfWeek.MONDAY, Serializer.INSTANCE.deserialize("\"monday\"", DayOfWeek.class));
        assertEquals(DayOfWeek.FUTURE_VALUE, Serializer.INSTANCE.deserialize("\"blah\"", DayOfWeek.class));
    }

}
