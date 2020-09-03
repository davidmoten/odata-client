package com.github.davidmoten.ms.dynamics;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.microsoft.dynamics.Dynamics;
import com.github.davidmoten.msgraph.builder.internal.MsGraphConstants;

import microsoft.dynamics.crm.container.System;

public class DynamicsMain {

    public static void main(String[] args) {

        // build client
        System client = Dynamics //
                .service(System.class) //
                .baseUrl("https://SOLUTION.api.crm4.dynamics.com/api/data/v9.1/") //
                .tenantName("TENANT_NAME") //
                .resource("https://SOLUTION.crm4.dynamics.com") //
                .scope(MsGraphConstants.SCOPE_MS_GRAPH_DEFAULT) //
                .clientId("CLIENT_ID") //
                .clientSecret("CLIENT_SECRET") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();

        // now use client
        client //
                .accounts() //
                .metadataMinimal() //
                .stream() //
                .limit(10) //
                .forEach(java.lang.System.out::println);

    }

}
