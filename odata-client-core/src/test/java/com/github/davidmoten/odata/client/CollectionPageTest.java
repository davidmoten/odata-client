package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

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
            public ResponseGet getResponseGET(List<PathItem> path) {
                return new ResponseGet(200, json);
            }
        };
        Context context = new Context(serializer, service);
        Optional<CollectionPage<Person>> c = CollectionPage.nextPage(json, Person.class, context);
        assertTrue(c.isPresent());
        assertEquals(2, c.get().currentPage().size());
        assertEquals("Russell", c.get().currentPage().get(0).firstName);
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
