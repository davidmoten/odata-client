package com.github.davidmoten.msgraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.odata.client.StreamProvider;

import odata.msgraph.client.complex.EmailAddress;
import odata.msgraph.client.complex.ItemBody;
import odata.msgraph.client.complex.Recipient;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.request.MailFolderRequest;
import odata.msgraph.client.enums.BodyType;

public class MsGraphMain {

    public static void main(String[] args) {

        // this system integration test
        // counts the messages in Drafts folder
        // adds a new message to the Drafts folder
        // changes the subject of the new message
        // checks that the count of messages has increased by one
        // deletes the message

        GraphService client = MsGraph //
                .tenantName(System.getProperty("tenantName")) //
                .clientId(System.getProperty("clientId")) //
                .clientSecret(System.getProperty("clientSecret")) //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();
        
        
        client.users().stream().forEach(user -> System.out.println(user.getUserPrincipalName()));
        
        
        System.exit(0);
        System.out.println(client.users().top(200).skip(100).get().currentPage().size());

        String mailbox = System.getProperty("mailbox");
        MailFolderRequest drafts = client //
                .users(mailbox) //
                .mailFolders("Drafts");

        // test streaming of DriveItem.content
        String user = System.getProperty("mailbox");
//        client.users(user).drive().items("01N6X7VZ6TXOGWV354WND3QHDNUYRXKVVH").get();
//        for (DriveItem item : client //
//                .users(user) //
//                .drive() //
//                .root() //
//                .children() //
//                .id("Attachments") //
//                .children() //
//                .metadataFull() //
//                .get()) {
//            // read content and count bytes
//            StreamProvider stream = item.getContent().get();
//            item.getUnmappedFields().entrySet().forEach(System.out::println);
//            System.out.println(stream.contentType());
//            byte[] bytes = toBytes(stream);
//            System.out.println("read " + item.getName().orElse("?") + " size=" + bytes.length);
//        }

        // count number of messages in Drafts
        long count = drafts.messages() //
                .metadataNone() //
                .get() //
                .stream() //
                .count(); //

        // Prepare a new message
        String id = UUID.randomUUID().toString().substring(0, 6);
        Message m = Message.builderMessage() //
                .subject("hi there " + id) //
                .body(ItemBody.builder() //
                        .content("hello there how are you") //
                        .contentType(BodyType.TEXT).build()) //
                .from(Recipient.builder() //
                        .emailAddress(EmailAddress.builder() //
                                .address(mailbox) //
                                .build())
                        .build())
                .build();

        // Create the draft message
        Message saved = drafts.messages().post(m);
        System.out.println("saved=" + saved);

        // change subject
        // Would have been nice to do like this but patch from the 
        // path associated with saved is not supported.
        // saved.withSubject("new subject " + id).patch();
        client //
                .users(mailbox) //
                .messages(saved.getId().get()) //
                .patch(saved.withSubject("new subject " + id));

        // check the subject has changed by reloading the message
        String amendedSubject = drafts //
                .messages(saved.getId().get()) //
                .get() //
                .getSubject() //
                .get();
        if (!("new subject " + id).equals(amendedSubject)) {
            throw new RuntimeException("subject not amended");
        }

        long count2 = drafts //
                .messages() //
                .metadataNone() //
                .get() //
                .stream() //
                .count(); //
        if (count2 != count + 1) {
            throw new RuntimeException("unexpected count");
        }

        // Delete the draft message
        drafts //
                .messages(saved.getId().get()) //
                .delete();

    }

    private static byte[] toBytes(StreamProvider stream) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        try (InputStream is = stream.get()) {
            int n;
            while ((n = is.read(b)) != -1) {
                bytes.write(b, 0, n);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

}
