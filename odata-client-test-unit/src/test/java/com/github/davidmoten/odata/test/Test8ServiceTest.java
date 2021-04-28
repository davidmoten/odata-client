package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.StreamProvider;

import test8.a.container.Test8Service;

public class Test8ServiceTest {

    @Test
    public void testGetStreamPropertyWhenMetadataNotPresent() {
        Test8Service client = Test8Service //
                .test() //
                .expectRequest("/Things/123") //
                .withMethod(HttpMethod.GET) //
                .withResponseStatusCode(200) //
                .withRequestHeadersStandard() //
                .withResponse("/response-thing.json") //
                .build();
        assertFalse(client.things(123).get().getPhoto().isPresent());
    }

    @Test
    public void testGetStreamPropertyWhenMetadataPresent() {
        Test8Service client = Test8Service //
                .test() //
                .expectRequest("/Things/123") //
                .withMethod(HttpMethod.GET) //
                .withResponseStatusCode(200) //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_FULL, //
                        RequestHeader.ODATA_VERSION) //
                .withResponse("/response-thing-full-metadata.json") //
                .build();
        Optional<StreamProvider> photo = client.things(123).metadataFull().get().getPhoto();
        assertTrue(photo.isPresent());
        try {
            // not pretty to use a try-catch but oh well
            photo.get().getBytes();
            Assert.fail();
        } catch (Throwable t) {
            assertTrue(t.getMessage().contains("response not found for url=https://thephoto,"));
        }
    }

    @Test
    public void testGetStreamPropertyWhenBase64Present() {
        Test8Service client = Test8Service //
                .test() //
                .expectRequest("/Things/123") //
                .withMethod(HttpMethod.GET) //
                .withResponseStatusCode(200) //
                .withRequestHeadersStandard() //
                .withResponse("/response-thing-inline-photo.json") //
                .build();
        Optional<StreamProvider> photo = client.things(123).get().getPhoto();
        assertTrue(photo.isPresent());
        assertEquals("hello", photo.get().getStringUtf8());
    }

}
