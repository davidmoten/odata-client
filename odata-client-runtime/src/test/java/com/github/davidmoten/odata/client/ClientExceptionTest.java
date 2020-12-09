package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class ClientExceptionTest {

    @Test
    public void testFromClientException() {
        ClientException e = new ClientException(123, "hello");
        ClientException e2 = ClientException.from(e);
        assertEquals(e, e2);
    }

    @Test
    public void testFromNonClientException() {
        IOException t = new IOException("boo");
        ClientException e = ClientException.from(t);
        assertEquals(t, e.getCause());
    }

}
