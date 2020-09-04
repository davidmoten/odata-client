package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.microsoft.authentication.GraphConstants;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.Builder5;
import com.github.davidmoten.msgraph.builder.GraphExplorerHttpService;

import odata.msgraph.client.container.GraphService;

public final class MsGraph {

    private static final String MSGRAPH_1_0_BASE_URL = "https://graph.microsoft.com/v1.0";

    private MsGraph() {
        // prevent instantiation
    }

    public static MicrosoftClientBuilder.Builder3<GraphService> tenantName(String tenantName) {
        return new MicrosoftClientBuilder<> 
                (MSGRAPH_1_0_BASE_URL, GraphService::new) //
                .tenantName(tenantName) //
                .resource(GraphConstants.RESOURCE_MS_GRAPH) //
                .scope(GraphConstants.SCOPE_MS_GRAPH_DEFAULT);
    }

    public static Builder5<GraphService> explorer() {
        return MsGraph //
                .tenantName("unused") //
                .clientId("unused") //
                .clientSecret("unused") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .accessTokenProvider(() -> "{token:https://graph.microsoft.com/}") //
                .httpServiceTransformer(GraphExplorerHttpService::new);
    }
}
