package com.github.davidmoten.msgraph;

import static com.github.davidmoten.odata.client.internal.Util.odataTypeNameFromAny;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.HttpRequestOptions;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;

import odata.msgraph.client.complex.EmailAddress;
import odata.msgraph.client.complex.Identity;
import odata.msgraph.client.complex.IdentitySet;
import odata.msgraph.client.complex.InvitationParticipantInfo;
import odata.msgraph.client.complex.ItemBody;
import odata.msgraph.client.complex.PasswordCredential;
import odata.msgraph.client.complex.Recipient;
import odata.msgraph.client.complex.ServiceHostedMediaConfig;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Application;
import odata.msgraph.client.entity.Attachment;
import odata.msgraph.client.entity.Call;
import odata.msgraph.client.entity.Contact;
import odata.msgraph.client.entity.Device;
import odata.msgraph.client.entity.DirectoryObject;
import odata.msgraph.client.entity.DriveItem;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.Group;
import odata.msgraph.client.entity.ItemAttachment;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.User;
import odata.msgraph.client.enums.BodyType;
import odata.msgraph.client.enums.Importance;
import odata.msgraph.client.enums.Modality;

public class GraphServiceTest {

    @Test
    public void testFileAttachmentBuilderCompiles() {
        FileAttachment.builderFileAttachment().build();
    }

    @Test
    public void testGetEntityWithComplexTypeCollection() {
        GraphService client = createClient("/users/1", "/response-user.json", //
                RequestHeader.ODATA_VERSION, //
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL);
        User user = client.users("1").get();
        assertEquals("Conf Room Adams", user.getDisplayName().get());
        assertEquals(1, user.getBusinessPhones().currentPage().size());
        assertEquals("+61 2 1234567", user.getBusinessPhones().currentPage().get(0));
    }
    
    @Test
    public void testGetEntityWithSelectBuilder() {
        GraphService client = createClient("/users/1?$select=displayName%2CbusinessPhones", //
        		"/response-user-select.json", //
                RequestHeader.ODATA_VERSION, //
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL);
        User user = client.users("1").selectBuilder().displayName().businessPhones().build().get();
        assertEquals("Conf Room Adams", user.getDisplayName().get());
        assertEquals(1, user.getBusinessPhones().currentPage().size());
        assertEquals("+61 2 1234567", user.getBusinessPhones().currentPage().get(0));
    }

    @Test
    public void testGetEntityCollectionWithoutNextPage() {
        GraphService client = createClient("/users", "/response-users.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        assertNotNull(client.users().get());
        CollectionPage<User> c = client.users().get();
        assertNotNull(c);
        assertEquals(31, c.currentPage().size());
        assertFalse(c.nextPage().isPresent());
    }

    static final class MyPage<T> {
        @JsonProperty(value="@odata.nextLink")
        final String nextLink;
        @JsonProperty(value="value")
        final List<T> value;
        
        MyPage( List<T> value, String nextLink) {
            this.nextLink = nextLink;
            this.value = value;
        }
    }
    
    @Test
    public void testGetEntityCollectionWithMaxPageSize() {
        int maxPageSize = 50;
        GraphService client = createClient("/users", "/response-users.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION,
                RequestHeader.maxPageSize(maxPageSize));
        CollectionPage<User> c = client.users().maxPageSize(maxPageSize).get();
        assertNotNull(c);
        assertEquals(31, c.currentPage().size());
    }

    @Test
    public void testGetEntityCollectionWithTimeouts() {
        int maxPageSize = 50;
        GraphService client = createClient("/users", "/response-users.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION,
                RequestHeader.maxPageSize(maxPageSize));
        CollectionPage<User> c = client //
        		.users() //
        		.maxPageSize(maxPageSize) //
        		.connectTimeout(10, TimeUnit.SECONDS) //
        		.readTimeout(30, TimeUnit.SECONDS) //
        		.get();
        assertNotNull(c);
        assertEquals(31, c.currentPage().size());
    }
    
    @Test
    public void testGetEntityCollectionWithNextPage() {
        GraphService client = clientBuilder() //
                .expectResponse("/me/contacts", "/response-contacts.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                // TODO what request header should be specified for next page?
                .expectResponse("/me/contacts?$skip=10", "/response-contacts-next-page.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        CollectionPage<Contact> c = client.me().contacts().get();
        assertNotNull(c);
        assertEquals(10, c.currentPage().size());
        assertEquals("https://graph.microsoft.com/v1.0/me/contacts?$skip=10", c.nextLink().get());
        assertTrue(c.nextPage().isPresent());
        Optional<String> nextLink = c.nextLink();
        c = c.nextPage().get();
        assertEquals(10, c.currentPage().size());
        assertEquals("Justin", c.currentPage().get(9).getGivenName().get());
        assertEquals("Justin", client //
                .me() //
                .contacts() //
                .urlOverride(nextLink.get()) //
                .get() //
                .currentPage() //
                .get(9) //
                .getGivenName() //
                .get());
    }
    
    @Test
    public void testSerializeShouldNotIncludeNulls() {
        User user = User.builderUser().id("12345").build();
        String json = Serializer.INSTANCE.serialize(user);
        assertEquals("{\"@odata.type\":\"microsoft.graph.user\",\"id\":\"12345\"}", json);
    }
    
    @Test
    public void testJsonMinimal() throws IOException {
        GraphService client = clientBuilder() //
                .expectResponse("/me/contacts", "/response-contacts.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                // TODO what request header should be specified for next page?
                .expectResponse("/me/contacts?$skip=10", "/response-contacts-next-page.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        CollectionPage<Contact> c = client.me().contacts().get();
        assertNotNull(c);
        ObjectMapper m = new ObjectMapper();
        try (InputStream expected = GraphServiceTest.class.getResourceAsStream("/response-contacts-minimal-json.json")) {
            assertEquals(m.readTree(expected), m.readTree(c.toJsonMinimal()));
        }
        assertTrue(Serializer.INSTANCE.serializePrettyPrint(c).contains("@odata.nextLink"));
    }

    @Test
    public void testGetCollectionGetNextLink() {
        GraphService client = clientBuilder() //
                .expectResponse("/me/contacts", "/response-contacts.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                // TODO what request header should be specified for next page?
                .expectResponse("/me/contacts?$skip=10", "/response-contacts-next-page.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        CollectionPage<Contact> c = client.me().contacts().get();
        assertNotNull(c);
        assertEquals(10, c.currentPage().size());
        assertEquals("https://graph.microsoft.com/v1.0/me/contacts?$skip=10", c.nextLink().get());
    }
    
    @Test
    public void testGetCollectionUsingSkip() {
        GraphService client = clientBuilder() //
                .expectResponse("/me/contacts?$skip=3&$top=200", "/response-contacts.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                // TODO what request header should be specified for next page?
                .expectResponse("/me/contacts?$skip=10", "/response-contacts-next-page.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        CollectionPage<Contact> c = client.me().contacts().top(200).skip(3).get();
        assertNotNull(c);
        assertEquals(10, c.currentPage().size());
    }
    
    @Test
    public void testGetCollectionThrowsInformativeError() {
        GraphService client = clientBuilder() //
                .expectResponse("/users", "/response-get-collection-error.json", HttpMethod.GET,
                        403, RequestHeader.ACCEPT_JSON_METADATA_MINIMAL,
                        RequestHeader.ODATA_VERSION) //
                .build();
        try {
            client.users().get().currentPage();
            Assert.fail();
        } catch (ClientException e) {
            assertTrue(e.getMessage().contains("Insufficient privileges"));
            assertEquals(403, (int) e.getStatusCode().get());
        }
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testCollectionPageRequestIsIterable() {
        //don't run, just check that compiles
        if (false) {
            GraphService client = clientBuilder().build();
            for (Application a : client.applications()) {
                // do nothing
            }
        }
    }
    
    @Test
    public void testGetCollectionUrlOverride() {
        GraphService client = clientBuilder() //
                .expectResponse("/me/contacts?$skipToken=ABC", "/response-contacts.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        CollectionPage<Contact> c = client.me().contacts().urlOverride("https://graph.microsoft.com/v1.0/me/contacts?$skipToken=ABC").get();
        assertNotNull(c);
        assertEquals(10, c.currentPage().size());
    }

    @Test
    public void testGetCollectionWithSelect() {
        GraphService client = clientBuilder() //
                .expectResponse("/groups?$select=id%2CgroupTypes", "/response-groups-select.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        CollectionPage<Group> c = client.groups().select("id,groupTypes").get();
        assertNotNull(c);
        assertEquals(49, c.currentPage().size());
        assertEquals("02bd9fd6-8f93-4758-87c3-1fb73740a315", c.currentPage().get(0).getId().get());
    }
    
    @Test
    public void testGetEntityWithNestedComplexTypesAndEnumDeserialisationAndUnmappedFields() {
        GraphService client = createClient("/me/messages/1", "/response-message.json",
                RequestHeader.ODATA_VERSION, //
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL);
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
    public void testActionDirectoryObjectsGetByIds() {
        //TODO it is unknown yet whether the path segment should be /microsoft.graph.getByIds or /getByIds. GraphExplorer expects /getByIds
        // test built according to https://docs.microsoft.com/en-us/graph/api/directoryobject-getbyids?view=graph-rest-1.0&tabs=http
        GraphService client = clientBuilder() //
                .expectRequestAndResponse("/directoryObjects/getByIds", //
                        "/request-directory-objects-get-by-ids.json", //
                        "/response-directory-objects-get-by-ids.json", //
                        HttpMethod.POST, //
                        200, //
                        RequestHeader.ODATA_VERSION, //
                        RequestHeader.CONTENT_TYPE_JSON, //
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL) //
                .build();
        CollectionPage<DirectoryObject> page = client.directoryObjects()
                .getByIds(Arrays.asList("a", "b"), Arrays.asList("c", "d")).get();
        assertTrue(page.currentPage().get(0).getId().get().startsWith("84b80893"));
    }

    @Test
    public void testEntityCollectionNotFromEntityContainer() {
        GraphService client = createClient("/me/messages/1/attachments",
                "/response-me-messages-1-attachments.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        List<Attachment> list = client.me().messages("1").attachments().get().toList();
        assertEquals(16, list.size());
    }

    @Test
    public void testDeserializationOfAttachmentEntityReturnsFileAttachment() {
        GraphService client = createClient("/me/messages/1/attachments/2",
                "/response-attachment.json", RequestHeader.ODATA_VERSION, //
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL);
        Attachment m = client.me().messages("1").attachments("2").get();
        assertTrue(m instanceof FileAttachment);
        FileAttachment f = (FileAttachment) m;
        assertEquals(6762, f.getContentBytes().get().length);
        assertEquals("lamp_thin.png", f.getContentId().get());
    }
    
	@Test
	public void testIssue28DoesNotThrowNPE() {
		Application application = Application.builderApplication() //
				.passwordCredentials(PasswordCredential.builder() //
						.secretText("Application secret Text") //
						.build()) //
				.build();
		application.getPasswordCredentials();
	}
	
	@Test
	public void testCoverageOfHttpRequestOptionsOnPropertyCollection() {
		Application application = Application.builderApplication() //
				.passwordCredentials(PasswordCredential.builder() //
						.secretText("Application secret Text") //
						.build()) //
				.build();
		assertEquals(1,
				application.getPasswordCredentials(
						HttpRequestOptions.connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS))
						.currentPage().size());
	}
    
    @Test
    public void testGetNestedCollectionWhichTestsContextPathSetWithIdInFirstCollection() {
        GraphService client = clientBuilder() //
                .expectResponse(
                        "/users/fred/mailFolders/inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime",
                        "/response-messages-expand-attachments-minimal-metadata.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .expectResponse(
                        "/users/fred/mailFolders/inbox/messages/AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEJAAAiIsqMbYjsT5e-T7KzowPTAAAYbvZDAAA%3D/attachments",
                        "/response-message-attachments.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .filter("isRead eq false") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .iterator() //
                .next();
        assertEquals(Arrays.asList("lamp_thin.png"), m.getAttachments().stream()
                .map(x -> x.getName().orElse("?")).collect(Collectors.toList()));
    }

    @Test
    public void testGetStreamOnItemAttachment() throws IOException {
        GraphService client = clientBuilder() //
                .expectResponse(
                        "/users/fred/mailFolders/Inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime",
                        "/response-messages-with-item-attachment.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .expectResponse("/users/fred/mailFolders/Inbox/messages/86/attachments",
                        "/response-attachments.json", RequestHeader.ACCEPT_JSON_METADATA_FULL,
                        RequestHeader.ODATA_VERSION) //
                .expectResponse(
                        "/users/fred/mailFolders/Inbox/messages/86/attachments/123/%24value",
                        "/response-item-attachment-raw.txt") //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .iterator() //
                .next();
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
    public void testTimeoutsOnNonEntityCollection() {
        GraphService client = clientBuilder() //
                .expectResponse(
                        "/users/fred/mailFolders/Inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime",
                        "/response-messages-with-item-attachment.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .expectResponse("/users/fred/mailFolders/Inbox/messages/86/attachments",
                        "/response-attachments.json", RequestHeader.ACCEPT_JSON_METADATA_FULL,
                        RequestHeader.ODATA_VERSION) //
                .expectResponse(
                        "/users/fred/mailFolders/Inbox/messages/86/attachments/123/%24value",
                        "/response-item-attachment-raw.txt") //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .iterator() //
                .next();
        assertEquals(1, m.getToRecipients(HttpRequestOptions.EMPTY).toList().size());
    }
    
    @Test
    public void testCollectionTypesHonourInheritance() {
        GraphService client = clientBuilder() //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1",
                        "/response-message-has-item-attachment.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_FULL) //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1/attachments",
                        "/response-attachments-includes-item.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL) //
                .build();
        List<Attachment> list = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages("1") //
                .metadataFull() //
                .get() //
                .getAttachments() //
                .toList();
        assertEquals(2, list.size());
        assertTrue(list.get(0) instanceof ItemAttachment);
        assertTrue(list.get(1) instanceof FileAttachment);
    }
    
    @Test
    public void testCollectionWithDerivedType() {
        GraphService client = clientBuilder() //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1",
                        "/response-message-has-item-attachment.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_FULL) //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1/attachments/microsoft.graph.itemAttachment",
                        "/response-attachments-one-item.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL) //
                .build();
        List<ItemAttachment> list = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages("1") //
                .metadataFull() //
                .get() //
                .getAttachments() //
                .filter(ItemAttachment.class)
                .toList();
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof ItemAttachment);
    }
    
    @Test
    public void testCollectionWithDerivedTypeFilterAvailableInNextBuilder() {
        GraphService client = clientBuilder() //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1",
                        "/response-message-has-item-attachment.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_FULL) //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1/attachments/microsoft.graph.itemAttachment",
                        "/response-attachments-one-item.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_NONE) //
                .build();
        List<ItemAttachment> list = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages("1") //
                .metadataFull() //
                .get() //
                .getAttachments() //
                .metadataNone() //
                .filter(ItemAttachment.class)
                .toList();
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof ItemAttachment);
    }

    @Test
    public void testUnmappedFields() {
        GraphService client = clientBuilder() //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1",
                        "/response-message-has-item-attachment.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_FULL) //
                .expectResponse("/users/fred/mailFolders/inbox/messages/1/attachments",
                        "/response-attachments-includes-item.json", RequestHeader.ODATA_VERSION,
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL) //
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
    public void testFunctionBoundToCollection() {
        GraphService client = clientBuilder() //
                .expectRequestAndResponse(
                        "/users/fred/mailFolders/inbox/messages/delta?$filter=receivedDateTime%2Bge%2B12345&$orderBy=receivedDateTime%2Bdesc",
                        "/request-messages-delta.json", //
                        "/response-messages-delta.json", //
                        HttpMethod.POST, //
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, //
                        RequestHeader.CONTENT_TYPE_JSON, //
                        RequestHeader.ODATA_VERSION) //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .delta() //
                .filter("receivedDateTime+ge+12345") //
                .orderBy("receivedDateTime+desc") //
                .metadataMinimal() //
                .iterator() //
                .next();
        assertEquals("86", m.getId().get());
    }

    @Test
    public void testMailMove() {
        // TODO get real json to use for this test
        GraphService client = clientBuilder() //
                .expectResponse(
                        "/users/fred/mailFolders/inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime&$expand=attachments",
                        "/response-messages-expand-attachments-minimal-metadata.json",
                        RequestHeader.ODATA_VERSION, RequestHeader.ACCEPT_JSON_METADATA_MINIMAL) //
                .expectRequestAndResponse(
                        "/users/fred/mailFolders/inbox/messages/AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEJAAAiIsqMbYjsT5e-T7KzowPTAAAYbvZDAAA%3D/move", //
                        "/request-post-action-move.json", //
                        "/response-message-move.json", //
                        HttpMethod.POST, //
                        HttpURLConnection.HTTP_CREATED, //
                        RequestHeader.ODATA_VERSION, //
                        RequestHeader.ACCEPT_JSON_METADATA_FULL, //
                        RequestHeader.CONTENT_TYPE_JSON) //
                .build();
        Message m = client //
                .users("fred") //
                .mailFolders("inbox") //
                .messages() //
                .filter("isRead eq false") //
                .expand("attachments") //
                .orderBy("createdDateTime") //
                .metadataMinimal() //
                .iterator() //
                .next();
        Message m2 = m.move("Archive").metadataFull().get().value();
        assertEquals(m.getId(), m2.getId());
    }

    @Test
    public void testMailRead() {

        GraphService client = clientBuilder() //
                .expectResponse(
                        "/users/fred/mailFolders/inbox/messages?$filter=isRead%20eq%20false&$orderBy=createdDateTime&$expand=attachments",
                        "/response-messages-expand-attachments-minimal-metadata.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .expectRequest(
                        "/users/fred/mailFolders/inbox/messages/AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEJAAAiIsqMbYjsT5e-T7KzowPTAAAYbvZDAAA%3D",
                        "/request-patch-message-is-read.json", HttpMethod.PATCH,
                        RequestHeader.CONTENT_TYPE_JSON,
                        RequestHeader.ODATA_VERSION, RequestHeader.ACCEPT_JSON) //
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
    }
    
    @Test
    @Ignore
    public void testToMethodCompilesWithLambda() {
    	GraphService client = clientBuilder().build();
    	client.users().to(x ->x).currentPage();
    }
    
    @Test
    @Ignore
    //TODO implement
    public void testChunkedUpload() {
        GraphService client = clientBuilder().build();
        DriveItem item = client.drives("123").items("abc").metadataNone().get();
        byte[] bytes = "1234567890".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        item.putChunkedContent().get().upload(in, bytes.length, 2);
    }
    
    @Test
    public void testGetODataNameFromEntity() {
    	assertEquals("microsoft.graph.message", odataTypeNameFromAny(Message.class));
    }
    
    @Test
    public void testGetODataNameFromComplexType() {
    	assertEquals("microsoft.graph.device", odataTypeNameFromAny(Device.class));
    }

    @Test
    @Ignore
    public void testSendEmailCompiles() {
// // This is how msgraph-java-sdk does it:
//    	Message message = new Message();
//    	message.subject = "Meet for lunch?";
//    	ItemBody body = new ItemBody();
//    	body.contentType = BodyType.TEXT;
//    	body.content = "The new cafeteria is open.";
//    	message.body = body;
//    	LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
//    	Recipient toRecipients = new Recipient();
//    	EmailAddress emailAddress = new EmailAddress();
//    	emailAddress.address = "fannyd@contoso.onmicrosoft.com";
//    	toRecipients.emailAddress = emailAddress;
//    	toRecipientsList.add(toRecipients);
//    	message.toRecipients = toRecipientsList;
//    	LinkedList<Recipient> ccRecipientsList = new LinkedList<Recipient>();
//    	Recipient ccRecipients = new Recipient();
//    	EmailAddress emailAddress1 = new EmailAddress();
//    	emailAddress1.address = "danas@contoso.onmicrosoft.com";
//    	ccRecipients.emailAddress = emailAddress1;
//    	ccRecipientsList.add(ccRecipients);
//    	message.ccRecipients = ccRecipientsList;
//
//    	boolean saveToSentItems = false;
//
//    	graphClient.me()
//    		.sendMail(message,saveToSentItems)
//    		.buildRequest()
//    		.post();
    	GraphService client = clientBuilder().build();
		Message message = Message //
				.builderMessage() //
				.subject("Meet for lunch?") //
				.body(ItemBody.builder() //
						.contentType(BodyType.TEXT) // 
						.content("The new cafeteria is open.") //
						.build()) //
				.toRecipients(Recipient.builder() //
						.emailAddress(EmailAddress.builder() //
								.address("fannd@contosolonnmicrosoft.com").build()).build()) //
				.ccRecipients(Recipient.builder() //
						.emailAddress(EmailAddress.builder() //
								.address("danas@contoso.onmicrosoft.com").build()).build()) //
				.build();
		client.me().sendMail(message, false).call();
    }
    
    
    @Test
    @Ignore
    public void testCallCompiles() {
        GraphService client = clientBuilder().build();
        Identity user = Identity //
                .builder() //
                .displayName("John") //
                .id("blah") //
                .build();
        IdentitySet set = IdentitySet //
                .builder() //
                .user(user) //
                .build();
        InvitationParticipantInfo targets = InvitationParticipantInfo //
                .builder()//
                .identity(set) //
                .build();
        ServiceHostedMediaConfig config = ServiceHostedMediaConfig //
                .builderServiceHostedMediaConfig() //
                .build();
        Call call = Call.builderCall() //
                .callbackUri("https://bot.contoso.com/callback") //
                .targets(Collections.singletonList(targets)) //
                .requestedModalities(Collections.singletonList(Modality.AUDIO)) //
                .mediaConfig(config) //
                .build();
        client.communications().calls().post(call);
    }

    // test paged complex type
    //

    private static ContainerBuilder<GraphService> clientBuilder() {
        return GraphService //
                .test() //
                .baseUrl("https://graph.microsoft.com/v1.0") //
                .pathStyle(PathStyle.IDENTIFIERS_AS_SEGMENTS) //
                .addProperties(MsGraphClientBuilder.createProperties());
    }

    private static GraphService createClient(String path, String resource,
            RequestHeader... requestHeaders) {
        return clientBuilder() //
                .expectResponse(path, resource, requestHeaders) //
                .build();
    }
}
