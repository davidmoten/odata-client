package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class UtilTest {

    @Test
    public void testBufferIterator() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Iterator<List<Integer>> it = Util.buffer(list.iterator(), 2);
        assertTrue(it.hasNext());
        assertEquals(Arrays.asList(1, 2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Arrays.asList(3, 4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Arrays.asList(5), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testBufferIteratorEmpty() {
        Iterator<List<Object>> it = Util.buffer(Collections.emptyIterator(), 2);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testBufferIteratorCallNextAfterFinishThrows() {
        Iterator<List<Object>> it = Util.buffer(Collections.emptyIterator(), 2);
        it.next();
    }

    @Test
    public void testBufferStream() {
        List<List<Integer>> list = Util.buffer(Stream.of(1, 2, 3, 4, 5), 2)
                .collect(Collectors.toList());
        assertEquals(3, list.size());
        assertEquals(Arrays.asList(1, 2), list.get(0));
        assertEquals(Arrays.asList(3, 4), list.get(1));
        assertEquals(Arrays.asList(5), list.get(2));
    }

}
