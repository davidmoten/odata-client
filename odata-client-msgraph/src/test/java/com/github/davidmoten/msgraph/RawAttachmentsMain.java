package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.ItemAttachment;

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

        System.out.println(client.sites("root").get().getDisplayName());

        System.exit(0);
        // test raw value of service
//        String s = client.users(mailbox) //
//                .mailFolders("Inbox") //
//                .messages() //
//                .filter("isRead eq false") //
//                .metadataFull() //
//                .get() //
//                .stream() //
//                .findFirst() //
//                .get() //
//                .getStream() //
//                .get() //
//                .getStringUtf8();
//        System.out.println(s);

        client //
                .users(mailbox) //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false and startsWith(subject, 'test contact')") //
                .get() //
                .stream() //
                .peek(x -> System.out.println(x.getSubject().orElse(""))) //
                .flatMap(x -> x.getAttachments().metadataFull().get().stream()) //
                .filter(x -> x instanceof ItemAttachment) //
                .map(x -> (ItemAttachment) x) //
                .map(x -> x.getStream().get().getStringUtf8()) //
                .peek(System.out::println) //
                .findFirst();
        client //
                .users(mailbox) //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false") //
                .get() //
                .stream() //
                .filter(x -> x.getHasAttachments().orElse(false)) //
                .peek(x -> System.out.println("Subject: " + x.getSubject().orElse(""))) //
                .flatMap(x -> x.getAttachments().get().stream()) //
                .peek(x -> System.out.println(
                        "  " + x.getName().orElse("?") + " [" + x.getSize().orElse(0) + "]")) //
                .count();

    }

}
