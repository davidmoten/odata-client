package com.github.davidmoten.odata.client.generator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilTest {

    @Test
    public void test() {
        assertEquals("microsoft.graph.targetResource",
                Util.replaceAlias("graph", "microsoft.graph", "graph.targetResource"));
    }

    @Test
    public void test2() {
        assertEquals("microsoft.graph.call",
                Util.replaceAlias("graph", "microsoft.graph", "microsoft.graph.call"));
    }

    @Test
    public void testCollectionReplace() {
        assertEquals("Collection(microsoft.graph.call)",
                Util.replaceAlias("graph", "microsoft.graph", "Collection(graph.call)"));
    }

    @Test
    public void testDontReplaceIfAliasHasExtraQualifier() {
        assertEquals("graph.extra.call",
                Util.replaceAlias("graph", "microsoft.graph", "graph.extra.call"));
    }

    @Test
    public void testCollectionDontReplaceIfAliasHasExtraQualifier() {
        assertEquals("Collection(graph.extra.call)",
                Util.replaceAlias("graph", "microsoft.graph", "Collection(graph.extra.call)"));
    }
}
