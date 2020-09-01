package com.github.davidmoten.ms.dynamics;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.microsoft.analytics.Analytics;

import microsoft.dynamics.crm.container.System;

public class DynamicsMain {

    public static void main(String[] args) {
        
        // build client
        System client = Analytics //
                .service(System.class) //
                .baseUrl("https://SOLUTION.api.crm4.dynamics.com/api/data/v9.1/") //
                .tenantName("TENANT_NAME") //
                .clientId("CLIENT_ID") //
                .clientSecret("CLIENT_SECRET") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .build();
        
        // now use client
        client.applicationusers().metadataMinimal().stream().limit(10)
                .forEach(java.lang.System.out::println);

    }

}
