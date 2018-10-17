package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;

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
        client //
                .users("dnex001@amsa.gov.au") //
                .mailFolders() //
                .get() //
                .forEach(x -> System.out.println(x.getDisplayName().orElse("?")));
    }

}
