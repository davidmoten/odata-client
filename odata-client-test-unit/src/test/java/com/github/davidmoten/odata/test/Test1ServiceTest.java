package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.github.davidmoten.guavamini.Sets;
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
        Product p = Product.builder().name("bingo").build();
        assertTrue(p.getChangedFields().toSet().isEmpty());
        p = p.withName(Optional.of("joey")).withID(Optional.of(124));
        assertEquals(Sets.newHashSet("Name", "ID"), p.getChangedFields().toSet());
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

    @Test
    public void testDelete() {
        Test1Service client = Test1Service //
                .test() //
                .expectDelete("/Products/1") //
                .build();
        client.products().id("1").delete();
    }

}
