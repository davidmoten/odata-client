package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.odata.client.internal.ChangedFields;

public class CollectionPageTest {

    @Test
    public void testParseCollectionResponse() throws IOException, URISyntaxException {
        String json = new String(
                Files.readAllBytes(Paths.get(CollectionPageTest.class
                        .getResource("/odata-paged-collection-response.json").toURI())),
                StandardCharsets.UTF_8);
        Serializer serializer = Serializer.DEFAULT;
        Service service = new Service() {

            @Override
            public HttpResponse GET(String url, Map<String, String> requestHeaders) {
                return new HttpResponse(200, json);
            }

            @Override
            public Path getBasePath() {
                return new Path("https://base", PathStyle.IDENTIFIERS_AS_SEGMENTS);
            }

            @Override
            public HttpResponse PATCH(String url, Map<String, String> requestHeaders, String text) {
                return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, "");
            }

            @Override
            public HttpResponse PUT(String url, Map<String, String> requestHeaders, String text) {
                return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, "");
            }

            @Override
            public HttpResponse POST(String url, Map<String, String> h, String text) {
                return new HttpResponse(HttpURLConnection.HTTP_CREATED, "");
            }

            @Override
            public HttpResponse DELETE(String url, Map<String, String> requestHeaders) {
                return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, "");
            }

        };
        SchemaInfo schemaInfo = new SchemaInfo() {

            @Override
            public Class<? extends ODataEntity> getEntityClassFromTypeWithNamespace(String name) {
                return Person.class;
            }

        };

        Context context = new Context(serializer, service);
        CollectionPageEntity<Person> c = serializer.deserializeCollectionPageEntity(json, Person.class,
                new ContextPath(context, service.getBasePath()), schemaInfo);
        assertEquals(2, c.values().size());
        assertEquals("Russell", c.values().get(0).firstName);
    }

    static final class Person implements ODataEntity {

        @JsonProperty("UserName")
        public String userName;

        @JsonProperty("FirstName")
        String firstName;

        @JsonProperty("LastName")
        String lastName;

        @Override
        public Map<String, Object> getUnmappedFields() {
            return Collections.emptyMap();
        }

        @Override
        public ChangedFields getChangedFields() {
            return ChangedFields.EMPTY;
        }
    }

}
