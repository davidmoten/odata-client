package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.odata.client.internal.ChangedFields;

public class CollectionPageTest {

    @Test
    public void testParseCollectionResponse() throws IOException, URISyntaxException {
        String json = new String(
                Files.readAllBytes(Paths.get(CollectionPageTest.class
                        .getResource("/odata-paged-collection-response.json").toURI())),
                StandardCharsets.UTF_8);
        Serializer serializer = Serializer.INSTANCE;
        HttpService service = createHttpService(json);
        SchemaInfo schemaInfo = name -> Person.class;

        Context context = new Context(serializer, service, Arrays.asList(schemaInfo));
        CollectionPage<Person> c = serializer.deserializeCollectionPage(json, Person.class,
                new ContextPath(context, service.getBasePath()), Collections.emptyList(),
                HttpRequestOptions.EMPTY, x -> {
                });
        assertEquals(2, c.currentPage().size());
        assertEquals("Russell", c.currentPage().get(0).firstName);
    }

    private static HttpService createHttpService(String json) {
        return new HttpService() {

            @Override
            public HttpResponse get(String url, List<RequestHeader> requestHeaders,
                    HttpRequestOptions options) {
                return new HttpResponse(200, json);
            }

            @Override
            public Path getBasePath() {
                return new Path("https://base", PathStyle.IDENTIFIERS_AS_SEGMENTS);
            }

            @Override
            public HttpResponse patch(String url, List<RequestHeader> requestHeaders,
                    InputStream content, int length, HttpRequestOptions options) {
                return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, "");
            }

            @Override
            public HttpResponse put(String url, List<RequestHeader> requestHeaders,
                    InputStream content, int length, HttpRequestOptions options) {
                return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, "");
            }

            @Override
            public HttpResponse post(String url, List<RequestHeader> h, InputStream content,
                    int length, HttpRequestOptions options) {
                return new HttpResponse(HttpURLConnection.HTTP_CREATED, "");
            }

            @Override
            public HttpResponse delete(String url, List<RequestHeader> requestHeaders,
                    HttpRequestOptions options) {
                return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, "");
            }

            @Override
            public void close() throws Exception {
                // do nothing
            }

            @Override
            public InputStream getStream(String url, List<RequestHeader> requestHeaders,
                    HttpRequestOptions options) {
                throw new UnsupportedOperationException();
            }

            @Override
            public InputStream getStream(HttpMethod method, String url,
                    List<RequestHeader> requestHeaders, HttpRequestOptions options) {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Test
    public void testUnmappedFields() throws IOException, URISyntaxException {
        String json = new String(
                Files.readAllBytes(Paths.get(
                        CollectionPageTest.class.getResource("/collection-page.json").toURI())),
                StandardCharsets.UTF_8);
        Serializer serializer = Serializer.INSTANCE;
        HttpService service = createHttpService(json);
        SchemaInfo schemaInfo = name -> Person.class;
        Context context = new Context(serializer, service, Lists.newArrayList(schemaInfo));
        ContextPath contextPath = new ContextPath(context,
                new Path("https://blah", PathStyle.IDENTIFIERS_AS_SEGMENTS));
        CollectionPage<Person> page = serializer.deserializeCollectionPage(json, Person.class,
                contextPath, Collections.emptyList(), HttpRequestOptions.EMPTY, p -> {
                });
        assertEquals(Lists.newArrayList(1, 2, 3), page.unmappedFields().get("hello"));

        // test serialization of unmapped fields
        String json2 = Serializer.INSTANCE.serialize(page);
        assertTrue(json2.contains("hello"));
    }

    static final class Person implements ODataEntityType {

        @JsonProperty("UserName")
        public String userName;

        @JsonProperty("FirstName")
        String firstName;

        @JsonProperty("LastName")
        String lastName;

        @Override
        public UnmappedFields getUnmappedFields() {
            return UnmappedFields.EMPTY;
        }

        @Override
        public ChangedFields getChangedFields() {
            return new ChangedFields();
        }

        @Override
        public void postInject(boolean addKeysToContextPath) {
            // do nothing
        }

        @Override
        public String odataTypeName() {
            return "person";
        }
    }
}
