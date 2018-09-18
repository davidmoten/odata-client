package com.github.davidmoten.msgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.davidmoten.odata.client.CollectionPageEntity;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.Service;
import com.github.davidmoten.odata.client.TestingService;
import com.github.davidmoten.odata.client.TestingService.Builder;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.User;

public class GraphServiceTest {

    @Test
    public void testGetEntityWithComplexTypeCollection() {
        GraphService client = createClient("/users/1", "/response-user.json");
        User user = client.users("1").get();
        assertEquals("Conf Room Adams", user.getDisplayName().get());
        assertEquals(1, user.getBusinessPhones().values().size());
        assertEquals("+61 2 1234567", user.getBusinessPhones().values().get(0));
    }

    @Test
    public void testGetEntityCollectionWithoutNextPage() {
        GraphService client = createClient("/users", "/response-users.json");
        assertNotNull(client.users().get());
        CollectionPageEntity<User> c = client.users().get();
        assertNotNull(c);
        assertEquals(31, c.currentPage().size());
        assertFalse(c.nextPage().isPresent());
    }

    @Test
    public void testGetEntityCollectionWithNextPage() {
        GraphService client = client(serviceBuilder().replyWithResource("/me/contacts", "/response-contacts.json"));
        assertNotNull(client.me().contacts().get());
        CollectionPageEntity<User> c = client.users().get();
        assertNotNull(c);
        assertEquals(10, c.currentPage().size());
        assertTrue(c.nextPage().isPresent());
    }

    private GraphService client(Builder b) {
        return new GraphService(new Context(Serializer.DEFAULT, b.build()));
    }

    private static TestingService.Builder serviceBuilder() {
        return TestingService //
                .baseUrl("https://graph.microsoft.com/v1.0") //
                .pathStyle(PathStyle.IDENTIFIERS_AS_SEGMENTS);
    }

    private static GraphService createClient(String path, String resource) {
        Service service = serviceBuilder() //
                .replyWithResource(path, resource) //
                .build();
        return new GraphService(new Context(Serializer.DEFAULT, service));
    }
}
