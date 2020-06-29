package com.github.davidmoten.odata.client.internal;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class ChecksTest {
    
    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(Checks.class);
    }

}
