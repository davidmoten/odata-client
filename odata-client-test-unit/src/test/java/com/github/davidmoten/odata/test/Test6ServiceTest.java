package com.github.davidmoten.odata.test;

import org.junit.Test;

import com.github.davidmoten.odata.client.ActionRequest;

import test6.a.action.request.RelatedProductsActionRequest;

public class Test6ServiceTest {
    
    @Test
    public void testActionRequestHasRightReturnType() {
        RelatedProductsActionRequest p = null;
        @SuppressWarnings("unused")
        ActionRequest<String> r = p;
    }

}
