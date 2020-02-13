package com.github.davidmoten.odata.test;

import org.junit.Test;

import com.github.davidmoten.guavamini.Lists;

import test6.a.entity.Product;

public class Test6ServiceTest {
    
    @SuppressWarnings("unused")
    @Test
    public void testActionApiCompiles() {
        Product p = null;
        if (false) {
            String answer = p.relatedProducts(123, Lists.newArrayList(10,20,30)) //
            .select("id") //
            .expand("attachments") //
            .metadataFull() //
            .call();
        }
    }

}
