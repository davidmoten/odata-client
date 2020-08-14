package com.github.davidmoten.odata.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Util {

    public static <T> T nvl(T object, T ifNull) {
        if (object == null) {
            return ifNull;
        } else {
            return object;
        }
    }

    static byte[] toByteArray(InputStream in) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        try {
            while ((n = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    public static String utf8(InputStream in) {
        return new String(toByteArray(in), StandardCharsets.UTF_8);
    }

    public static <T> Stream<List<T>> buffer(Stream<T> stream, int size) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(buffer(stream.iterator(), size), 0), false);
    }

    public static <T> Iterator<List<T>> buffer(Iterator<T> it, int size) {
        return new Iterator<List<T>>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public List<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                List<T> list = new ArrayList<>();
                for (long v = 0; v < size && hasNext(); v++) {
                    list.add(it.next());
                }
                return list;
            }
        };
    }
    
    static <S extends Collection<T>, T> S add(Iterable<T> iterable, S collection) {
    	Iterator<T> it = iterable.iterator();
        while (it.hasNext()) {
            collection.add(it.next());
        }
        return collection;
    }
}
