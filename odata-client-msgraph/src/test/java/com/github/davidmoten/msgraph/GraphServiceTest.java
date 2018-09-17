package com.github.davidmoten.msgraph;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.junit.Test;

import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.ResponseGet;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.Service;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.User;

public class GraphServiceTest {

    @Test
    public void testGetEntity() {
        Service service = new Service() {

            @Override
            public ResponseGet getResponseGET(String url, Map<String, String> requestHeaders) {
                String text;
                try {
                    text = new String(
                            Files.readAllBytes(
                                    new File("src/test/resources/response-user.json").toPath()),
                            StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return new ResponseGet(200, text);
            }

            @Override
            public Path getBasePath() {
                return new Path("https://boo.com", PathStyle.IDENTIFIERS_AS_SEGMENTS);

            }
        };
        Serializer serializer = new Serializer() {
        };
        Context c = new Context(serializer, service);
        User user = new GraphService(c).users("1").get();
        assertEquals("Conf Room Adams", user.getDisplayName().get());
    }
}
