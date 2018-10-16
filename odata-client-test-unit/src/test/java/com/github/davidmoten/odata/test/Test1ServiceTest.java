package com.github.davidmoten.odata.test;

import org.junit.Test;

import com.github.davidmoten.odata.client.HttpMethod;

import test1.a.entity.Product;
import test1.b.container.Test1Service;

public class Test1ServiceTest {

    @Test
    public void testCanReferenceEntityFromEntityContainerInAnotherSchema() {
        Test1Service client = Test1Service.test().baseUrl("http://base").build();
        client.products(1);
    }
    
    @Test
    public void testChangedFieldsAreSet() {
    }
    
    @Test
    public void testPost() {
        Test1Service client = Test1Service //
                .test() //
                .baseUrl("http://base") //
                .expectRequest("/Products", "/request-post.json", HttpMethod.POST) //
                .replyWithResource("/Products", "/response-post.json", HttpMethod.POST) //
                .build();
        Product p = Product.builder().name("bingo").build();
        client.products().post(p);
    }
    
}
