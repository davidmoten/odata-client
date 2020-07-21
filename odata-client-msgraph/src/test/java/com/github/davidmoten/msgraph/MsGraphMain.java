package com.github.davidmoten.msgraph;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import odata.msgraph.client.complex.AttachmentItem;
import odata.msgraph.client.complex.EmailAddress;
import odata.msgraph.client.complex.ItemBody;
import odata.msgraph.client.complex.Recipient;
import odata.msgraph.client.complex.UploadSession;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.request.MailFolderRequest;
import odata.msgraph.client.enums.AttachmentType;
import odata.msgraph.client.enums.BodyType;

public class MsGraphMain {

    public static void main(String[] args) {

        GraphService client = MsGraph //
                .tenantName(System.getProperty("tenantName")) //
                .clientId(System.getProperty("clientId")) //
                .clientSecret(System.getProperty("clientSecret")) //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();
        {
            String mailbox = System.getProperty("mailbox");
            MailFolderRequest drafts = client //
                    .users(mailbox) //
                    .mailFolders("Drafts");
            Message m = Message.builderMessage() //
                    .subject("hi there " + new Date()) //
                    .body(ItemBody.builder() //
                            .content("hello there how are you") //
                            .contentType(BodyType.TEXT).build()) //
                    .from(Recipient.builder() //
                            .emailAddress(EmailAddress.builder() //
                                    .address(mailbox) //
                                    .build())
                            .build())
                    .build();
            m = drafts.messages().post(m);
            AttachmentItem a = AttachmentItem.builder().attachmentType(AttachmentType.FILE).contentType("text/plain")
                    .name("attachment.txt").size(5000000L).build();
            UploadSession session = client.users(mailbox).messages(m.getId().get()).attachments().createUploadSession(a).get();
            System.out.println(session.getUploadUrl());
            // {"@odata.context":"https://graph.microsoft.com/v1.0/$metadata#microsoft.graph.uploadSession","uploadUrl":"https://outlook.office.com/api/v2.0/Users('783d0188-1d5d-440b-944f-daa46ca1fcb0@7d14b925-2921-4d30-8f2f-a6a3c1245f6d')/Messages('AAMkADRiMjI2ZmQwLTJkZmUtNDQ0NS1iZGVmLWE1ZjBjYjBiYmUzZQBGAAAAAABCSTzSpYOXTJf5ExxfstMvBwD0q8bfcZ7QQry5SG484keOAAAACf7pAADh3MOyvFF4RJgQOjzBK-sdAANghbkOAAA=')/AttachmentSessions('AAMkADRiMjI2ZmQwLTJkZmUtNDQ0NS1iZGVmLWE1ZjBjYjBiYmUzZQBGAAAAAABCSTzSpYOXTJf5ExxfstMvBwD0q8bfcZ7QQry5SG484keOAAAACjNYAADh3MOyvFF4RJgQOjzBK-sdAANghRKsAAA=')?authtoken=eyJhbGciOiJSUzI1NiIsImtpZCI6IktmYUNIUlN6bllHMmNIdDRobk9JQnpndlU5MD0iLCJ4NXQiOiJKaGg0RkVpMnpsLUlFalBYQUQ1OVRmQzR0S0kiLCJ0eXAiOiJKV1QifQ.eyJyc2NvcGVsZW4iOiI0NTgiLCJ2ZXIiOiJSZXNvdXJjZUxvb3BiYWNrLkFwcC5WMSIsInJvbGVzIjoiQXR0YWNobWVudFNlc3Npb24uV3JpdGUiLCJyZXNvdXJjZV9zY29wZSI6IntcIlVSTFwiOlwidVBHQjR4RDNMY2o0bmFETkFDalh2dytEc2hWT1B3SmU5K3IxNU56VUxKMD1cIn0iLCJjb3JyaWQiOiIyZGRiMmE0NS0xNmRkLTQ5ZTUtYjJiNS1jYmE0MjcwMjUxMGYiLCJhcHBpZCI6IjAwMDAwMDAzLTAwMDAtMDAwMC1jMDAwLTAwMDAwMDAwMDAwMCIsImFwcGlkYWNyIjoiMCIsInRpZCI6IjdkMTRiOTI1LTI5MjEtNGQzMC04ZjJmLWE2YTNjMTI0NWY2ZCIsImlhdCI6MTU5NTMwOTE4NywibmJmIjoxNTk1MzA5MTg3LCJleHAiOjE1OTUzMTk5ODcsImlzcyI6Imh0dHBzOi8vcmVzb3VyY2Uuc2VsZi8iLCJhdWQiOiJodHRwczovL291dGxvb2sub2ZmaWNlLmNvbS9hcGkvIn0.Uv9G5JBfEqaj-RTlSynNQ2fuJSidC2qUIYbeA2TSpiQi_WGcmiyT2e7n47j8LOxu6fcY_mRAumzoCpXZ4vDfJQFVCQzw038WmsNvDbprEXzF10tPCZs1g02HeaTMPq1WW1KfraLMJrqKYJRSAPP6aAoqUu_dX4GKFtDaQXhCrlQHAb-gzHAGBDpSXGPrLaYfDgbtsTNQvfxKn1L6KLj-wsvPkrmYGP4rzjXrjrsU92QjXawtXsVTvP-cTxHvrmvQp0zd251iyx3vvjs08-yZ6rgFcgDQnfOpz1pzDuFgsZc-hHn3JgVV8xBeN0mSoRnr0HflXzBspGpQ51PL3ByA2Q","expirationDateTime":"2020-07-21T07:26:27.6837661Z","nextExpectedRanges":["0-"]}
        }
        
        System.exit(0);
        client.users().select("userPrincipalName").stream()
                .forEach(user -> System.out.println(user.getUserPrincipalName()));

        System.exit(0);
        {
            String mailbox = System.getProperty("mailbox");
            MailFolderRequest drafts = client //
                    .users(mailbox) //
                    .mailFolders("Drafts");

            // test streaming of DriveItem.content
            // String user = System.getProperty("mailbox");
            // client.users(user).drive().items("01N6X7VZ6TXOGWV354WND3QHDNUYRXKVVH").get();
            // for (DriveItem item : client //
            // .users(user) //
            // .drive() //
            // .root() //
            // .children() //
            // .id("Attachments") //
            // .children() //
            // .metadataFull() //
            // .get()) {
            // // read content and count bytes
            // StreamProvider stream = item.getContent().get();
            // item.getUnmappedFields().entrySet().forEach(System.out::println);
            // System.out.println(stream.contentType());
            // byte[] bytes = toBytes(stream);
            // System.out.println("read " + item.getName().orElse("?") + " size=" +
            // bytes.length);
            // }

            // this system integration test
            // counts the messages in Drafts folder
            // adds a new message to the Drafts folder
            // changes the subject of the new message
            // checks that the count of messages has increased by one
            // deletes the message

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
    }
}
