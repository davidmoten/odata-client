package com.github.davidmoten.odata.client;

import java.io.File;
import java.util.Collection;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.Schema;

final class Util {

    static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    static <T> Stream<T> types(Schema schema, Class<T> cls) {
        return filter(schema.getComplexTypeOrEntityTypeOrTypeDefinition(), cls);
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> filter(Collection<?> c, Class<T> cls) {
        return (Stream<T>) (c.stream() //
                .filter(x -> cls.isInstance(x)));
    }

}
