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
                Util.replaceAlias("microsoft.graph.call", "microsoft.graph", "microsoft.graph.call"));
    }
}
