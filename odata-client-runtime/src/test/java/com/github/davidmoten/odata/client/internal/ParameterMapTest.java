package com.github.davidmoten.odata.client.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

public class ParameterMapTest {
    
    @Test
    public void testNullValueInMapIssue423() {
        Map<String, TypedObject> map = ParameterMap
                .put("param1", "Edm.String", "blah")
                .put("param2", "Edm.String", null)
                .build();
        Map<String, Object> m = ParameterMap.toMap(map);
        assertEquals("blah", m.get("param1"));
        assertNull(m.get("param2"));
    }

}
