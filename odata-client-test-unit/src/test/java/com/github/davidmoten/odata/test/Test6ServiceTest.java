package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.HttpMethod;

import test6.a.container.Test6ServiceA;
import test6.a.entity.Product;
import test6.b.container.Test6Service;

public class Test6ServiceTest {

    @Test
    @Ignore
    public void testActionReturningCollectionApiCompiles() {
        // just has to compile, is not run!
        Product p = Mockito.mock(Product.class);
        @SuppressWarnings("unused")
        CollectionPage<String> rel = p.relatedProducts(123, Lists.newArrayList(10, 20, 30)) //
                .select("id") //
                .expand("attachments") //
                .metadataFull() //
                .get();
    }

    // @Test
    // @Ignore
    // public void testActionReturningNonCollectionApiCompiles() {
    // // just has to compile, is not run!
    // Product p = Mockito.mock(Product.class);
    // @SuppressWarnings("unused")
    // Integer answer = p.countRelatedProducts(123, Lists.newArrayList(10, 20, 30))
    // //
    // .select("id") //
    // .expand("attachments") //
    // .metadataFull() //
    // .get();
    // }

    @Test
    public void testEntityActionReturningBoolean() {
        // just has to compile, is not run!
        Test6Service client = Test6Service.test() //
                .expectResponse("/Products/1", "/response-product-1.json")
                .expectRequestAndResponse("/Products/1/Test6.A.revokeSessions", "/request-revoke-sessions.json",
                        "/response-revoke-sessions.json", HttpMethod.POST) //
                .build();
        assertTrue(client.products(1).get().revokeSessions().get().value());
    }

    @Test
    public void testEntityRequestActionReturningBoolean() {
        // just has to compile, is not run!
        Test6Service client = Test6Service.test() //
                .expectRequestAndResponse("/Products/1/Test6.A.revokeSessions", "/request-revoke-sessions.json",
                        "/response-revoke-sessions.json", HttpMethod.POST) //
                .build();
        assertTrue(client.products(1).revokeSessions().get().value());
    }
    
    @Test
    public void testFunctionParametersAreInlineSyntax() {
        Test6Service client = Test6Service.test() //
                .expectResponse("/Products/1/Test6.A.functionToTestNulls/(value%3D1%2Ccollection%3D%5B1%2C2%2C3%5D)", //
                        "/function-return-1.json")
                .build();
        int value = client.products(1).functionToTestNulls(1, Arrays.asList(1, 2, 3)).get().value();
        assertEquals(456, value);
    }
    
    @Test
    public void testFunctionParametersAreInlineSyntaxWhenNonCollectionParameterNull() {
        Test6Service client = Test6Service.test() //
                .expectResponse("/Products/1/Test6.A.functionToTestNulls/(value%3Dnull'Edm.Int32'%2Ccollection%3D%5B1%2C2%2C3%5D)", //
                        "/function-return-1.json")
                .build();
        int value = client.products(1).functionToTestNulls(null, Arrays.asList(1, 2, 3)).get().value();
        assertEquals(456, value);
    }
    
    @Test
    public void testFunctionParametersAreInlineSyntaxWhenCollectionParameterNull() {
        Test6Service client = Test6Service.test() //
                .expectResponse("/Products/1/Test6.A.functionToTestNulls/(value%3D1%2Ccollection%3Dnull'Collection(Edm.Int32)')", //
                        "/function-return-1.json")
                .build();
        int value = client.products(1).functionToTestNulls(1, null).get().value();
        assertEquals(456, value);
    }
    
    @Test
    public void testUnboundFunction() {
        Test6ServiceA client = Test6ServiceA.test() //
                .expectResponse("/Test6.A.globalFunction/(productId%3D%221%22%2Cvalue%3D23)", //
                        "/function-return-1.json")
                .build();
        assertEquals(456,(int) client.globalFunction("1", 23).get().value());
    }

}
