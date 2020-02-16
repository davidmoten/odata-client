package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.odata.client.HttpMethod;

import test6.a.entity.Product;
import test6.b.container.Test6Service;

public class Test6ServiceTest {

    @Test
    @Ignore
    public void testActionReturningCollectionApiCompiles() {
        // just has to compile, is not run!
        Product p = Mockito.mock(Product.class);
        @SuppressWarnings("unused")
        Collection<String> answer = p.relatedProducts(123, Lists.newArrayList(10, 20, 30)) //
                .select("id") //
                .expand("attachments") //
                .metadataFull() //
                .get();
    }

//    @Test
//    @Ignore
//    public void testActionReturningNonCollectionApiCompiles() {
//        // just has to compile, is not run!
//        Product p = Mockito.mock(Product.class);
//        @SuppressWarnings("unused")
//        Integer answer = p.countRelatedProducts(123, Lists.newArrayList(10, 20, 30)) //
//                .select("id") //
//                .expand("attachments") //
//                .metadataFull() //
//                .get();
//    }

    @Test
    public void testActionJustABindingParameterApiCompiles() {
        // just has to compile, is not run!
        Test6Service client = Test6Service.test() //
                .replyWithResource("/Products/1", "/response-product-1.json")
                .expectRequestAndReply("/Products/1/Test6.A.revokeSessions",
                        "/request-revoke-sessions.json", "/response-revoke-sessions.json", HttpMethod.POST) //
                .build();
        assertTrue(client.products(1).get().revokeSessions().get());
    }

}
