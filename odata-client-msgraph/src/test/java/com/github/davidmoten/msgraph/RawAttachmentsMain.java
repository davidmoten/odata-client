package com.github.davidmoten.msgraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.ItemAttachment;
import odata.msgraph.client.entity.request.MailFolderRequest;

public final class RawAttachmentsMain {

    public static void main(String[] args) {

        String tenantName = System.getProperty("tenantName");
        String clientId = System.getProperty("clientId");
        String clientSecret = System.getProperty("clientSecret");
        String mailbox = System.getProperty("mailbox");

        GraphService client = MsGraph //
                .tenantName(tenantName) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();

//        if (false) {
//            String url = "";
//            System.out.println(url + "\n->\n" + client._service().getStringUtf8(url));
//            System.exit(0);
//        }

        MailFolderRequest inbox = client //
                .users(mailbox) //
                .mailFolders("Inbox");

        inbox //
                .messages() //
                .filter("isRead eq false and startsWith(subject, 'test contact')") //
                // .expand("attachments")//
                .get() //
                .stream() //
                .peek(x -> System.out.println(x.getSubject().orElse(""))) //
                .flatMap(x -> x.getAttachments().metadataFull().get().stream()) //
                .filter(x -> x instanceof ItemAttachment) //
                .map(x -> (ItemAttachment) x) //
                .map(x -> x.getStream().get().getStringUtf8()) //
                .peek(System.out::println) //
                .findFirst();

    }

}
