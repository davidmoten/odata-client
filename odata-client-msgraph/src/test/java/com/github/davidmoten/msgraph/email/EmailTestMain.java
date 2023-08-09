package com.github.davidmoten.msgraph.email;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.Email;
import com.github.davidmoten.msgraph.Email.DraftMessage;
import com.github.davidmoten.msgraph.MsGraph;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.enums.BodyType;

public class EmailTestMain {

    public static void main(String[] args) throws InterruptedException {
        String tenantName = System.getProperty("tenant");
        String mailbox = System.getProperty("mailbox");
        String clientId = System.getProperty("clientId");
        String clientSecret = System.getProperty("clientSecret");
        GraphService client = MsGraph //
                .tenantName(tenantName) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .connectTimeout(60, TimeUnit.SECONDS) //
                .readTimeout(60, TimeUnit.SECONDS) //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();

        DraftMessage message = Email
            .mailbox(mailbox) 
            .subject("hi there " + new Date())
            .bodyType(BodyType.TEXT)
            .body("hello there how are you")
            .to("me@gmail.com")
            .create(client);
        
        message.send();
        message.send();
    }
}
