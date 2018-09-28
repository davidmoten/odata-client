package com.github.davidmoten.msgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.davidmoten.odata.client.CollectionPageEntity;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Attachment;
import odata.msgraph.client.entity.Contact;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.User;
import odata.msgraph.client.enums.Importance;

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
        assertEquals(31, c.values().size());
        assertFalse(c.nextPage().isPresent());
    }

    @Test
    public void testGetEntityCollectionWithNextPage() {
        GraphService client = serviceBuilder() //
                .replyWithResource("/me/contacts", "/response-contacts.json") //
                .replyWithResource("/me/contacts?$skip=10", "/response-contacts-next-page.json") //
                .build();
        CollectionPageEntity<Contact> c = client.me().contacts().get();
        assertNotNull(c);
        assertEquals(10, c.values().size());
        assertTrue(c.nextPage().isPresent());
        c = c.nextPage().get();
        assertEquals(10, c.values().size());
        assertEquals("Justin", c.values().get(9).getGivenName().get());
    }

    @Test
    public void testGetEntityWithNestedComplexTypesAndEnumDeserialisationAndUnmappedFields() {
        GraphService client = createClient("/me/messages/1", "/response-message.json");
        Message m = client.me().messages("1").get();
        assertTrue(m.getSubject().get().startsWith("MyAnalytics"));
        assertEquals("MyAnalytics", m.getFrom().get().getEmailAddress().get().getName().get());
        assertEquals(Importance.NORMAL, m.getImportance().get());
        assertEquals(2, m.getUnmappedFields().size());
        assertEquals("W/\"CQAAABYAAAAiIsqMbYjsT5e/T7KzowPTAAEMTBu8\"", m.getUnmappedFields().get("@odata.etag"));
        assertEquals(
                "https://graph.microsoft.com/v1.0/$metadata#users('48d31887-5fad-4d73-a9f5-3c356e68a038')/messages/$entity",
                m.getUnmappedFields().get("@odata.context"));
    }

    @Test
    public void testEntityCollectionNotFromEntityContainer() {
        GraphService client = createClient("/me/messages/1/attachments", "/response-me-messages-1-attachments.json");
        CollectionPageEntity<Attachment> m = client.me().messages("1").attachments().get();
    }

    @Test
    public void testDeserializationOfAttachmentEntityReturnsFileAttachment() {
        GraphService client = createClient("/me/messages/1/attachments/2", "/response-attachment.json");
        Attachment m = client.me().messages("1").attachments("2").get();
        assertTrue(m instanceof FileAttachment);
        FileAttachment f = (FileAttachment) m;
        assertEquals(6762, f.getContentBytes().get().length);
        assertEquals("lamp_thin.png", f.getContentId().get());
    }

    // test paged complex type
    //

    private static ContainerBuilder<GraphService> serviceBuilder() {
        return GraphService //
                .test() //
                .baseUrl("https://graph.microsoft.com/v1.0") //
                .pathStyle(PathStyle.IDENTIFIERS_AS_SEGMENTS);
    }

    private static GraphService createClient(String path, String resource) {
        return serviceBuilder() //
                .replyWithResource(path, resource) //
                .build();
    }
}
