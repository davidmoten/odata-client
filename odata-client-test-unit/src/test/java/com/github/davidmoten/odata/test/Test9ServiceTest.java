package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.odata.client.HttpMethod;

import test9.a.container.Test9Service;

public class Test9ServiceTest {
    
    @Test
    public void testEntitySetForEntityWithNoKeyReturnsRequestForOneEntityOnly() {
        Test9Service client = Test9Service //
                .test() //
                .expectRequest("/Things") //
                .withMethod(HttpMethod.GET) //
                .withResponseStatusCode(200) //
                .withRequestHeadersStandard() //
                .withResponse("/response-thing-no-key.json") //
                .build();
        assertEquals("Fred", client.things().metadataMinimal().get().getName().orElse(""));
    }
    
    @Test
    public void testSingletontForEntityWithNoKeyReturnsRequestForOneEntityOnly() {
        Test9Service client = Test9Service //
                .test() //
                .expectRequest("/Thing") //
                .withMethod(HttpMethod.GET) //
                .withResponseStatusCode(200) //
                .withRequestHeadersStandard() //
                .withResponse("/response-thing-no-key.json") //
                .build();
        assertEquals("Fred", client.thing().metadataMinimal().get().getName().orElse(""));
    }


}
