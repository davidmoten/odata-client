package com.github.davidmoten.odata.test;

import java.io.InputStream;

import org.junit.Test;

import test5.container.Test5Service;

public class Test5ServiceTest {

    @Test
    public void testStreamGet() {
        Test5Service client = Test5Service.test() //
                .replyWithResource("/Products/1/Photo/$value", "/response-get-stream.txt") //
                .build();
        try (InputStream is = client.products(1).get().getPhoto().get().get()) {
            
        };
    }

}
