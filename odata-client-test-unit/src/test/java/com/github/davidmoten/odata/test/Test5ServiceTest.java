package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.github.davidmoten.odata.client.StreamProvider;

import test5.container.Test5Service;

public class Test5ServiceTest {

    @Test
    public void testStreamGet() throws IOException {
        Test5Service client = Test5Service.test() //
                .replyWithResource("/Products/2", "/response-get-stream.json") //
                .replyWithResource("/Photos(123)/%24value", "/response-get-stream.txt") //
                .build();
        StreamProvider stream = client.products(2).metadataFull().get().getStream().get();
        assertEquals("image/jpeg",stream.contentType());
        try (InputStream is = stream.get()) {
            byte[] bytes = getBytes(is);
            assertEquals("some bytes", new String(bytes));
        }
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

}
