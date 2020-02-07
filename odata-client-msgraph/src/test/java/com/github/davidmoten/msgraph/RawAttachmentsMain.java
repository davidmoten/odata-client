package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;

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

        
        client //
                .users(mailbox) //
                .mailFolders("Inbox") //
                .messages() 
                .filter("isRead eq false") 
                .expand("attachments") 
                .get() 
                .stream() 
                .map(x -> x.getSubject().orElse("")) 
                .forEach(System.out::println);
    }

}
