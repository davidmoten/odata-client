package com.github.davidmoten.microsoft.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class ClientCredentialAccessTokenProviderTest {
    
    @Test
    public void testExtract() {
        String message = "Server returned HTTP response code: 400 for URL: https://login.microsoftonline.com/100255.onmicrosoft.com/oauth2/token";
        Optional<Integer> code = ClientCredentialsAccessTokenProvider.extractStatusCode(message);
        assertTrue(code.isPresent());
        assertEquals(400, (int) code.get());
    }

    @Test
    public void testExtractNotFound() {
        String message = "blah";
        Optional<Integer> code = ClientCredentialsAccessTokenProvider.extractStatusCode(message);
        assertFalse(code.isPresent());
    }
    
    @Test
    public void testExtractFromNull() {
        String message = null;
        Optional<Integer> code = ClientCredentialsAccessTokenProvider.extractStatusCode(message);
        assertFalse(code.isPresent());
    }
    
}
