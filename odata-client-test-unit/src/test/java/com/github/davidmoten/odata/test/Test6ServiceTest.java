package com.github.davidmoten.odata.test;

import java.util.Collection;

import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

import test6.a.entity.Product;

public class Test6ServiceTest {

    @SuppressWarnings("unused")
    @Test
    public void testActionReturningCollectionApiCompiles() {
        Product p = null;
        if (false) {
            Collection<String> answer = p.relatedProducts(123, Lists.newArrayList(10, 20, 30)) //
                    .select("id") //
                    .expand("attachments") //
                    .metadataFull() //
                    .get();
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testActionReturningNonCollectionApiCompiles() {
        Product p = null;
        if (false) {
            Integer answer = p.countRelatedProducts(123, Lists.newArrayList(10, 20, 30)) //
                    .select("id") //
                    .expand("attachments") //
                    .metadataFull() //
                    .get();
        }
    }

}
