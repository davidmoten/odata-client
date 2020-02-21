package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import test5.container.Test5Service;
import test5.entity.Product;
import test5.schema.SchemaInfo;

public class CustomRequestTest {

    @Test
    public void testCustomRequestGet() {
        Test5Service client = Test5Service.test() //
                .expectResponse("/Products/1", "/response-product-1.json") //
                .build();
        Product p = client._custom().get("https://testing.com/Products/1", Product.class, SchemaInfo.INSTANCE);
        System.out.println(p);
        assertEquals(1, (int) p.getID().get());
    }
}
