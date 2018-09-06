package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeTest {

    @Test
    public void serializeComplexTypeWithOptional() throws JsonProcessingException {
        microsoft.graph.generated.complex.Package p = new microsoft.graph.generated.complex.Package();
        p.setType(Optional.of("fred"));
        ObjectMapper m = Serialization.MAPPER;
        assertEquals("{\"type\":\"fred\"}", m.writeValueAsString(p));
        p.setType(Optional.empty());
        assertEquals("{\"type\":null}", m.writeValueAsString(p));
    }

}
