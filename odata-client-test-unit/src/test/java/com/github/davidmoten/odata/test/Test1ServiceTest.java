package com.github.davidmoten.odata.test;

import org.junit.Test;

import test1.container.Test1Service;

public class Test1ServiceTest {

    @Test
    public void testTopLevelCollection() {
         Test1Service client = Test1Service.test().baseUrl("http://base").build();
         client.products("1");
    }

    
}
