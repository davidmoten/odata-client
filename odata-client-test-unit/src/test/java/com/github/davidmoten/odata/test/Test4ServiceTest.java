package com.github.davidmoten.odata.test;

import org.junit.Test;

import test4.container.Test4Service;

public class Test4ServiceTest {

    @Test
    public void testGeneratesMultipleIdParameters() {
        Test4Service client = Test4Service.test().build();
        client.products(1, "fred");
    }
}
