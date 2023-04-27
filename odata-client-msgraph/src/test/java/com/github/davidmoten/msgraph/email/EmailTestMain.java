package com.github.davidmoten.msgraph.email;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.MsGraph;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Attachment;
import odata.msgraph.client.entity.FileAttachment;

public class EmailTestMain {

    public static void main(String[] args) {
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

        client.users(mailbox) //
                .mailFolders("inbox") //
                .messages() //
                .stream() //
                .limit(10) //
                .forEach(x -> System.out
                        .println(x.getSubject().orElse("unknown subject") + "\n" + x.getId().orElse("")));
    }

}
