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
    public void testBasePathWithTwoQueries() {
        assertEquals("http://base?x=true&y=3",
                a.addQuery("x", "true").addQuery("y", "3").toString());
    }

    @Test
    public void testBasePathWithQueryIsEncoded() {
        assertEquals("http://base?x=ab%20cd", a.addQuery("x", "ab cd").toString());
    }

    @Test
    public void testPathDelimiterAppliedWhenStyleIsRoundBrackets() {
        assertEquals("http://base(age%3D23%2Cheight%3D186)", a.addKeys(new NameValue("age", "23"), new NameValue("height", "186")).toString());
    }
}
