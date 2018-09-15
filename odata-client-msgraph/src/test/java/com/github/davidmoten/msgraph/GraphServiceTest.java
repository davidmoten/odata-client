package com.github.davidmoten.msgraph;

import java.util.Map;

import org.junit.Test;

import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.ResponseGet;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.Service;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Contact;

public class GraphServiceTest {

    @Test
    public void testGetEntity() {
        Service service = new Service() {

            @Override
            public ResponseGet getResponseGET(String url, Map<String, String> requestHeaders) {
                return new ResponseGet(200, "{\"givenName\":\"fred\"}");
            }

            @Override
            public Path getBasePath() {
                return new Path("https://boo.com",PathStyle.IDENTIFIERS_AS_SEGMENTS);

            }};
        Serializer serializer = new Serializer() {};
        Context c = new Context(serializer, service);
        Contact contact = new GraphService(c).users("1").contacts("2").get();
    }
}
