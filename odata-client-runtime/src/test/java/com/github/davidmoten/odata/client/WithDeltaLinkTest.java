package com.github.davidmoten.odata.client;

import java.util.Optional;

import org.junit.Test;

public class WithDeltaLinkTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testNotBoth() {
        new ObjectOrDeltaLink<String>(Optional.of("hello"), Optional.of("blah"));
    }
    
    @Test
    public void testObjectPresent() {
        new ObjectOrDeltaLink<String>(Optional.of("hello"), Optional.empty());
    }
    
    @Test
    public void testObjectNotPresent() {
        new ObjectOrDeltaLink<String>(Optional.empty(), Optional.empty());
        new ObjectOrDeltaLink<String>(Optional.empty(), Optional.of("blah"));
    }

}
