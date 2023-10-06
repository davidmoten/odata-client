package com.github.davidmoten.msgraph.email;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.Email;
import com.github.davidmoten.msgraph.Email.DraftMessage;
import com.github.davidmoten.msgraph.MsGraph;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.enums.BodyType;

public class EmailTestMain {

    private static final boolean SEND = false;

    public static void main(String[] args) throws InterruptedException {
        String tenantName = System.getProperty("tenant");
        String sendMailbox = System.getProperty("sendMailbox");
        String receiveMailbox = System.getProperty("receiveMailbox");
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

        DraftMessage message = Email //
                .mailbox(sendMailbox) //
                .subject("hi there " + new Date()) //
                .bodyType(BodyType.TEXT) //
                .body("hello there how are you") //
                .to("me@gmail.com") //
                .create(client);
        
        if (SEND) {
            message.send();
        }

        long count = client //
                .users(receiveMailbox) //
                .mailFolders("inbox") //
                .messages() //
                .filter("isRead eq false") //
                .orderBy("createdDateTime") //
                .stream() //
                .limit(1000) //
                .map(x -> x.getSubject().orElse("?")) //
                .peek(System.out::println) //
                .count();
        System.out.println(count);
    }
}
