package com.github.davidmoten.msgraph.beta;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.builder.GraphExplorerHttpService;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder.Builder3;

import odata.msgraph.client.beta.container.GraphService;

public final class MsGraph {

    private static final String MSGRAPH_BETA_BASE_URL = "https://graph.microsoft.com/beta";

    private MsGraph() {
        // prevent instantiation
    }

    public static MsGraphClientBuilder.Builder<GraphService> tenantName(String tenantName) {
        return new MsGraphClientBuilder<GraphService>(MSGRAPH_BETA_BASE_URL,
                GraphService::new).tenantName(tenantName);
    }
    
    public static Builder3<GraphService> explorer() {
        return MsGraph //
                .tenantName("unused") //
                .clientId("unused") //
                .clientSecret("unused") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .accessTokenProvider(() -> "{token:https://graph.microsoft.com/}") //
                .httpServiceTransformer(GraphExplorerHttpService::new);
    }
}
