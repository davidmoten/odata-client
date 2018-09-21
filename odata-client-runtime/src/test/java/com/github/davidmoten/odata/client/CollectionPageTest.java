package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionPageTest {

    @Test
    public void testParseCollectionResponse() throws IOException, URISyntaxException {
        String json = new String(
                Files.readAllBytes(Paths.get(CollectionPageTest.class
                        .getResource("/odata-paged-collection-response.json").toURI())),
                StandardCharsets.UTF_8);
        Serializer serializer = new Serializer() {
        };
        Service service = new Service() {

            @Override
            public ResponseGet GET(String url, Map<String, String> requestHeaders) {
                return new ResponseGet(200, json);
            }

            @Override
            public Path getBasePath() {
                return new Path("https://base", PathStyle.IDENTIFIERS_AS_SEGMENTS);
            }
        };
        SchemaInfo schemaInfo = new SchemaInfo() {

            @Override
            public Class<? extends ODataEntity> getEntityClassFromTypeWithNamespace(String name) {
                return Person.class;
            }

        };

        Context context = new Context(serializer, service);
        CollectionPageEntity<Person> c = CollectionPageEntity.create(json, Person.class,
                new ContextPath(context, service.getBasePath()), schemaInfo);
        assertEquals(2, c.currentPage().size());
        assertEquals("Russell", c.currentPage().get(0).firstName);
    }

    static final class Person implements ODataEntity {

        @JsonProperty("UserName")
        public String userName;

        @JsonProperty("FirstName")
        String firstName;

        @JsonProperty("LastName")
        String lastName;
    }

}
