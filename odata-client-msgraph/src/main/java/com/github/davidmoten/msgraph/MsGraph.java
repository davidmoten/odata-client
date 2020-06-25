package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.builder.GraphExplorerHttpService;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder.Builder3;

import odata.msgraph.client.container.GraphService;

public final class MsGraph {

    private static final String MSGRAPH_1_0_BASE_URL = "https://graph.microsoft.com/v1.0";

    private MsGraph() {
        // prevent instantiation
    }

    public static MsGraphClientBuilder.Builder<GraphService> tenantName(String tenantName) {
        return new MsGraphClientBuilder<GraphService> //
        (MSGRAPH_1_0_BASE_URL, //
                context -> new GraphService(context)).tenantName(tenantName);
    }

    public static Builder3<GraphService> explorer() {
        return MsGraph //
                .tenantName("unused") //
                .clientId("unused") //
                .clientSecret("unused") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .accessTokenProvider(() -> "{token:https://graph.microsoft.com/}") //
                .httpServiceTransformer(s -> new GraphExplorerHttpService(s));
    }
}
