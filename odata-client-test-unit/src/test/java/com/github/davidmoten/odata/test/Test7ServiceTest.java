package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.davidmoten.odata.client.PathStyle;

import test7.container.Test7Service;
import test7.entity.Product;
import test7.entity.Thing;
import test7.schema.SchemaInfo;

public class Test7ServiceTest {
    
    @Test
    public void testPolymorphicSingleEntityPropertyWithExpand() {
        String baseUrl = "https://testing.com";
        PathStyle pathStyle = PathStyle.IDENTIFIERS_AS_SEGMENTS;
        Test7Service client = Test7Service.test() //
                .baseUrl(baseUrl) //
                .pathStyle(pathStyle) //
                .addSchema(SchemaInfo.INSTANCE) //
                .expectRequest("/Things")
                .withRequestHeadersStandard() //
                .withResponse("/response-thing-poly.json")
                .build();
        Thing a = client.things().get();
        assertTrue(a instanceof Product);
        Product p = (Product) a;
        assertEquals("fred", p.getName().get());
        assertEquals(123, (int) p.getID().get());
    }

}
