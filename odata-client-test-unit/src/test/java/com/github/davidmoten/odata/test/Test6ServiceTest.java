package com.github.davidmoten.odata.test;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.davidmoten.guavamini.Lists;

import test6.a.entity.Product;

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

    @Test
    @Ignore
    public void testActionReturningNonCollectionApiCompiles() {
        // just has to compile, is not run!
        Product p = Mockito.mock(Product.class);
        @SuppressWarnings("unused")
        Integer answer = p.countRelatedProducts(123, Lists.newArrayList(10, 20, 30)) //
                .select("id") //
                .expand("attachments") //
                .metadataFull() //
                .get();
    }
    
    @Test
    @Ignore
    public void testActionJustABindingParameterApiCompiles() {
        // just has to compile, is not run!
        Product p = Mockito.mock(Product.class);
        @SuppressWarnings("unused")
        Boolean answer = p.revokeSessions()
                .select("id") //
                .expand("attachments") //
                .metadataFull() //
                .get();
    }

}
