package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.odata.client.RequestHeader;

import test10.complex.SpecialContent;
import test10.container.Test10Service;
import test10.entity.Thing;

public class Test10ServiceTest {
    
    @Test
    @Ignore
    // TODO support ComplexType polymorphic deserializatio
    public void test() {
        Test10Service client = Test10Service.test() //
                .expectRequest("/Things")
                .withResponse("/response-complex-type-poly.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL,
                        RequestHeader.ODATA_VERSION) //
                .build();
        List<Thing> list = client.things().get().toList();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getContent().get() instanceof SpecialContent);
    }

}
 