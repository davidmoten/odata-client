package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.builder.AuthenticationEndpoint;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.RequestOptions;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.ItemAttachment;

public final class AdHocMain {

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
                .authenticationEndpoint(AuthenticationEndpoint.GLOBAL) // is default
                .build();
        
        client.users(mailbox).messages().get().currentPage().stream().forEach(System.out::println);

        // if (false) {
        // String url =
        // "https://graph.microsoft.com/v1.0/users('dnex001%40amsa.gov.au')/mailFolders('inbox')/messages('AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwACvGHoMgAAAA%3D%3D')/attachments('AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwACvGHoMgAAAAESABAAEk3MvTWvlkaZoyGmFgr4ag%3D%3D')";
        // System.out.println(url + "\n->\n" + client._service().getStringUtf8(url,
        // Arrays.asList(new RequestHeader("Accept",
        // "application/json;odata.metadata=full"))));
        // System.exit(0);
        // }
        
        String a = client._custom().getString("https://graph.microsoft.com/v1.0/users/dnex001%40amsa.gov.au/messages('AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwACvxQL4gAAAA==')/$value", RequestOptions.EMPTY, RequestHeader.ACCEPT_JSON, RequestHeader.ODATA_VERSION);

        System.out.println(a);
        System.exit(1);
        
        String s = client.users(mailbox).messages(
                "AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwACvxQL4gAAAA==") //
                .metadataFull() //
                .get() //
                .getStream() // get mime content of message
                .get() //
                .getStringUtf8();
        System.out.println(s);

        // System.out.println(client.users(mailbox).get().revokeSignInSessions());
        System.exit(0);

        System.out.println(client.sites("root").get().getDisplayName().orElse(""));

        // client.users(mailbox).get().revokeSignInSessions(null)

        // test raw value of service
        // String s = client.users(mailbox) //
        // .mailFolders("Inbox") //
        // .messages() //
        // .filter("isRead eq false") //
        // .metadataFull() //
        // .get() //
        // .stream() //
        // .findFirst() //
        // .get() //
        // .getStream() //
        // .get() //
        // .getStringUtf8();
        // System.out.println(s);

        client //
                .users(mailbox) //
                .mailFolders("Inbox") //
                .messages() //
                .filter("isRead eq false and startsWith(subject, 'test contact')") //
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
                .stream() //
                .filter(x -> x.getHasAttachments().orElse(false)) //
                .peek(x -> System.out.println("Subject: " + x.getSubject().orElse(""))) //
                .flatMap(x -> x.getAttachments().get().stream()) //
                .peek(x -> System.out.println(
                        "  " + x.getName().orElse("?") + " [" + x.getSize().orElse(0) + "]")) //
                .count();

    }

}
