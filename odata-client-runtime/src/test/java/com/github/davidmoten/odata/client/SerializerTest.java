package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;

import org.junit.Test;

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

}
