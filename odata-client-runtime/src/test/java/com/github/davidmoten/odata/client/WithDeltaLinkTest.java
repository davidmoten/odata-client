package com.github.davidmoten.odata.client;

import java.util.Optional;

import org.junit.Test;

public class WithDeltaLinkTest {
    
    @Test
    public void testNotBoth() {
        new ObjectOrDeltaLink<String>(Optional.of("hello"), Optional.of("blah"));
    }

}
