package com.github.davidmoten.msgraph;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.enums.Importance;

public class MsGraphMain {

    public static void main(String[] args) {
        System.setProperty("https.proxyHost", "proxy.amsa.gov.au");
        System.setProperty("https.proxyPort", "8080");

        GraphService client = MsGraph //
                .tenantName(System.getProperty("tenantName")) //
                .clientId(System.getProperty("clientId")) //
                .clientSecret(System.getProperty("clientSecret")) //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();

        String mailbox = "dnex001@amsa.gov.au";

//        client //
//                .users(mailbox) //
//                .mailFolders() //
//                .get() //
//                .forEach(x -> System.out.println(x.getDisplayName().orElse("?")));

        client.users(mailbox) //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false") //
                .expand("attachments") //
                .get() //
                .stream() //
                .map(x -> x.getSubject().orElse("")) //
                .forEach(System.out::println);
        
        System.exit(0);

        Message m = client //
                .users(mailbox) //
                .messages() //
                .filter("(receivedDateTime ge 2018-10-13T04:00:00Z) and (receivedDateTime le 2018-10-13T05:00:00Z)") //
                .get() //
                .currentPage().get(0);

        m = client //
                .users(mailbox) //
                .messages(m.getId().orElse("")) //
                .get();

        System.out.println(m.getId().orElse(""));
        System.out.println(m.getSubject().orElse(""));
        System.out.println(m.getBody().map(b -> b.getContent().orElse("")).orElse(""));
        // System.out.println(m.getUnmappedFields());

        m.withImportance(Optional.of(Importance.LOW)).put();

        System.out.println("=============\ndone");
    }

}
