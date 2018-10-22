package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import test2.b.container.Test2Service;
import test2.b.entity.Product;

public class Test2ServiceTest {

    @Test
    public void testCanReferenceComplexTypeFromEntityContainerInAnotherSchema() {
        Test2Service client = Test2Service.test().build();
        client.products(1);
        Product p = Product.builder().build();
        assertFalse(p.getAddress().isPresent());
    }

}
