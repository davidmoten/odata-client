package org.davidmoten.odata.client.maven;

import static org.davidmoten.odata.client.maven.GeneratorMojo.toPackage;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeneratorMojoTest {

    @Test
    public void testToPackage() {
        assertEquals("abc", toPackage("abc"));
        assertEquals("abc", toPackage("ABC"));
        assertEquals("abc", toPackage("abc.."));
        assertEquals("abc", toPackage("..abc"));
        assertEquals("abc.de", toPackage("abc.de"));
        assertEquals("abc_de", toPackage("abc_de"));
        assertEquals("abc.de123", toPackage("abc.de123"));
        assertEquals("abc.de123", toPackage("abc.de123;:=+"));
    }

}
