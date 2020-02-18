package com.github.davidmoten.msgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Attachment;
import odata.msgraph.client.entity.Contact;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.ItemAttachment;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.User;
import odata.msgraph.client.enums.Importance;

public class GraphServiceTest {

    @Test
    public void testGetEntityWithComplexTypeCollection() {
        GraphService client = createClient("/users/1", "/response-user.json");
        User user = client.users("1").get();
        assertEquals("Conf Room Adams", user.getDisplayName().get());
        assertEquals(1, user.getBusinessPhones().currentPage().size());
        assertEquals("+61 2 1234567", user.getBusinessPhones().currentPage().get(0));
    }

    @Test
    public void testGetEntityCollectionWithoutNextPage() {
        GraphService client = createClient("/users", "/response-users.json");
        assertNotNull(client.users().get());
        CollectionPage<User> c = client.users().get();
        assertNotNull(c);
        assertEquals(31, c.currentPage().size());
        assertFalse(c.nextPage().isPresent());
    }

    @Test
    public void testGetEntityCollectionWithNextPage() {
        GraphService client = serviceBuilder() //
                .replyWithResource("/me/contacts", "/response-contacts.json") //
                .replyWithResource("/me/contacts?$skip=10", "/response-contacts-next-page.json") //
                .build();
        CollectionPage<Contact> c = client.me().contacts().get();
        assertNotNull(c);
        assertEquals(10, c.currentPage().size());
        assertTrue(c.nextPage().isPresent());
        c = c.nextPage().get();
        assertEquals(10, c.currentPage().size());
        assertEquals("Justin", c.currentPage().get(9).getGivenName().get());
    }

    @Test
    public void testGetEntityWithNestedComplexTypesAndEnumDeserialisationAndUnmappedFields() {
        GraphService client = createClient("/me/messages/1", "/response-message.json");
        Message m = client.me().messages("1").get();
        assertTrue(m.getSubject().get().startsWith("MyAnalytics"));
        assertEquals("MyAnalytics", m.getFrom().get().getEmailAddress().get().getName().get());
        assertEquals(Importance.NORMAL, m.getImportance().get());
        assertEquals(Sets.newHashSet("@odata.etag", "@odata.context"),
                m.getUnmappedFields().keySet());
        assertEquals("W/\"CQAAABYAAAAiIsqMbYjsT5e/T7KzowPTAAEMTBu8\"",
                m.getUnmappedFields().get("@odata.etag"));
        assertEquals(
                "https://graph.microsoft.com/v1.0/$metadata#users('48d31887-5fad-4d73-a9f5-3c356e68a038')/messages/$entity",
                m.getUnmappedFields().get("@odata.context"));
    }

    @Test
    public void testEntityCollectionNotFromEntityContainer() {
        GraphService client = createClient("/me/messages/1/attachments",
                "/response-me-messages-1-attachments.json");
        List<Attachment> list = client.me().messages("1").attachments().get().toList();
        assertEquals(16, list.size());
    }

    @Test
    public void testDeserializationOfAttachmentEntityReturnsFileAttachment() {
        GraphService client = createClient("/me/messages/1/attachments/2",
                "/response-attachment.json");
        Attachment m = client.me().messages("1").attachments("2").get();
        assertTrue(m instanceof FileAttachment);
        FileAttachment f = (FileAttachment) m;
        assertEquals(6762, f.getContentBytes().get().length);
        assertEquals("lamp_thin.png", f.getContentId().get());
    }

    @Test
    public void testGetNestedCollectionWhichTestsContextPathSetWithIdInFirstCollection() {
        GraphService client = serviceBuilder() //
                .replyWithResource(
                        "/users/fred/mailFolders/inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime",
                        "/response-messages-expand-attachments-minimal-metadata.json") //
                .replyWithResource(
                        "/users/fred/mailFolders/inbox/messages/AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEJAAAiIsqMbYjsT5e-T7KzowPTAAAYbvZDAAA%3D/attachments",
                        "/response-message-attachments.json") //
                .build();
        CollectionPage<Message> messages = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .filter("isRead eq false") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .get();
        Message m = messages.iterator().next();
        assertEquals(Arrays.asList("lamp_thin.png"), m.getAttachments().stream()
                .map(x -> x.getName().orElse("?")).collect(Collectors.toList()));
    }

    @Test
    public void testGetStreamOnItemAttachment() throws IOException {
        GraphService client = serviceBuilder() //
                .replyWithResource(
                        "/users/fred/mailFolders/Inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime",
                        "/response-messages-with-item-attachment.json") //
                .replyWithResource("/users/fred/mailFolders/Inbox/messages/86/attachments",
                        "/response-attachments.json") //
                .replyWithResource(
                        "/users/fred/mailFolders/Inbox/messages/86/attachments/123/%24value",
                        "/response-item-attachment-raw.txt") //
                .build();
        CollectionPage<Message> messages = client //
                .users("fred") //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .get();
        Message m = messages.iterator().next();
        ItemAttachment a = (ItemAttachment) m //
                .getAttachments() //
                .metadataFull() //
                .stream() //
                .findFirst() //
                .get();
        String s = new String(Util.read(a.getStream().get().get()));
        assertEquals(60, s.length());
    }

    @Test
    public void testUnmappedFields() {
        GraphService client = serviceBuilder() //
                .replyWithResource("/users/fred/mailFolders/inbox/messages/1",
                        "/response-message-has-item-attachment.json") //
                .replyWithResource("/users/fred/mailFolders/inbox/messages/1/attachments",
                        "/response-attachments-includes-item.json") //
                .build();
        Attachment a = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages("1") //
                .metadataFull() //
                .get() //
                .getAttachments() //
                .stream() //
                .findFirst() //
                .get();
        String editLink = a.getUnmappedFields().get("@odata.editLink").toString();
        assertEquals("editLink1", editLink);
    }
    
    @Test
    @Ignore
    public void testFunctionBoundToCollection() {
        GraphService client = serviceBuilder() //
                .replyWithResource(
                        "/users/fred/mailFolders/inbox/messages/microsoft.graph.delta?$filter=receivedDateTime%2Bge%2B12345&$orderBy=receivedDateTime%2Bdesc",
                        "/response-messages-delta.json") //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .delta() //
                .filter("receivedDateTime+ge+12345") //
                .orderBy("receivedDateTime+desc") //
                .metadataMinimal() //
                .stream() //
                .iterator() //
                .next();
    }

    @Test
    public void testMailMove() {
        // TODO get real json to use for this test
        GraphService client = serviceBuilder() //
                .replyWithResource(
                        "/users/fred/mailFolders/inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime&$expand=attachments",
                        "/response-messages-expand-attachments-minimal-metadata.json") //
                .expectRequestAndReply(
                        "/users/fred/mailFolders/inbox/messages/AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEJAAAiIsqMbYjsT5e-T7KzowPTAAAYbvZDAAA%3D/microsoft.graph.move", //
                        "/request-post-action-move.json", //
                        "/response-message-move.json", //
                        HttpMethod.POST) //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .filter("isRead eq false") //
                .expand("attachments") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .get() // ;
                .iterator() //
                .next();
        Message m2 = m.move("Archive").metadataFull().get();
        assertEquals(m.getId(), m2.getId());
    }

    @Test
    public void testMailRead() {

        GraphService client = serviceBuilder() //
                .replyWithResource(
                        "/users/fred/mailFolders/inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime&$expand=attachments",
                        "/response-messages-expand-attachments-minimal-metadata.json") //
                .expectRequest(
                        "/users/fred/mailFolders/inbox/messages/AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEJAAAiIsqMbYjsT5e-T7KzowPTAAAYbvZDAAA%3D",
                        "/request-patch-message-is-read.json", HttpMethod.PATCH) //
                .build();
        
        Message m = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .filter("isRead eq false") //
                .expand("attachments") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .get() // ;
                .iterator() //
                .next();
        

        System.out.println(m.getSubject());
        // mark as read
        m.withIsRead(true).patch();
//        client.users("fred") //
//                .messages(m.getId().get()) //
//                .patch(m.withIsRead(true));

        // List<Option> queryOptions = Lists.newArrayList( //
        // new QueryOption("$filter", "isRead eq false"), //
        // new QueryOption("$expand", "attachments"), //
        // new QueryOption("$orderBy", "createdDateTime"));
        //
        // IMessageCollectionPage p = client //
        // .users(mailbox) //
        // .mailFolders("inbox") //
        // .messages() //
        // .buildRequest(queryOptions) //
        // .get();
        // List<Message> list = p.getCurrentPage();
        // while (true) {
        // if (!list.isEmpty()) {
        // log.info("msgraph returned " + list.size() + " in current page");
        // MessagReceiver sender = senderFactory.create(connectionFactory, queue);
        // sender.sendJmsMessages(list, client, mailbox);
        // monitoring.setProperty(MonitoringKey.LastReportTime,
        // System.currentTimeMillis());
        // }
        // if (p.getNextPage() == null) {
        // break;
        // }
        // p = p.getNextPage().buildRequest().get();
        // list = p.getCurrentPage();
        // }
    }

    // test paged complex type
    //

    private static ContainerBuilder<GraphService> serviceBuilder() {
        return GraphService //
                .test() //
                .baseUrl("https://graph.microsoft.com/v1.0") //
                .pathStyle(PathStyle.IDENTIFIERS_AS_SEGMENTS) //
                .addProperty("modify.stream.edit.link", "true");
    }

    private static GraphService createClient(String path, String resource) {
        return serviceBuilder() //
                .replyWithResource(path, resource) //
                .build();
    }
}
