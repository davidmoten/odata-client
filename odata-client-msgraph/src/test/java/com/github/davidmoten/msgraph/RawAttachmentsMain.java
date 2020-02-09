package com.github.davidmoten.msgraph;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.FileAttachment;
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

        if (false) {
            String url = "https://graph.microsoft.com/v1.0/users('dnex001%40amsa.gov.au')/mailFolders('Inbox')/messages('AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwACufIe7gAAAA%3D%3D')/attachments('AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwACufIe7gAAAAESABAAJiKY2F6di02zkDDZ7TpVag%3D%3D')/$value";
            System.out.println(url + "\n->\n" + client._service().getStringUtf8(url));
            System.exit(0);
        }

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
                .flatMap(x -> {
                    System.out.println("Subject=" + x.getSubject().orElse(""));
                    return x.getAttachments().metadataFull().get().stream();
                }) //
                .peek(x -> System.out
                        .println(x.getClass().getSimpleName() + " " + x.getName().orElse("?")))
                .filter(x -> x instanceof ItemAttachment) //
                .map(x -> (ItemAttachment) x) //
                .map(x -> {
                    try (InputStream in = x.getStream().get().get()) {
                        byte[] b = Util.read(in);
                        System.out.println(new String(b));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return x;
                })
                .map(x -> x.getClass().getSimpleName() + ": " + x.getName().orElse("?")
                        + " of content type " + x.getContentType().orElse("?")) //
                .findFirst();

    }

}
