package com.github.davidmoten.msgraph;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.StreamProvider;

import odata.msgraph.client.complex.EmailAddress;
import odata.msgraph.client.complex.ItemBody;
import odata.msgraph.client.complex.Recipient;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.DriveItem;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.request.DriveItemRequest;
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

        String mailbox = System.getProperty("mailbox");
        MailFolderRequest drafts = client //
                .users(mailbox) //
                .mailFolders("Drafts");

        String user = System.getProperty("email");
        client.users(user).drive().items("01N6X7VZ6TXOGWV354WND3QHDNUYRXKVVH").get();
        for (DriveItem item : client.users(user).drive().root().children().id("Attachments").children().metadataFull().get()) {
            System.out.println(item);
            
            StreamProvider stream = item.getContent().get();
            System.out.println(stream.contentType());
        }

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

        // change subject
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

}
