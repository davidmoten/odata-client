package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

public class PathTest {

    private final static Path a = new Path("http://base", Collections.emptyMap(),
            PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS);

    @Test
    public void testBasePathOnly() {
        assertEquals("http://base", a.toString());
    }

    @Test
    public void testBasePathWithOneSegment() {
        assertEquals("http://base/boo", a.addSegment("boo").toString());
    }

    @Test
    public void testBasePathWithOneSegmentIsEncoded() {
        assertEquals("http://base/boo%20", a.addSegment("boo ").toString());
    }

    @Test
    public void testBasePathWithQuery() {
        assertEquals("http://base?x=true", a.addQuery("x", "true").toString());
    }

    @Test
    public void testBasePathWithQueryIsEncoded() {
        assertEquals("http://base?x=ab%20cd", a.addQuery("x", "ab cd").toString());
    }

}
