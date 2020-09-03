package com.github.davidmoten.msgraph.beta;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.Builder3;
import com.github.davidmoten.msgraph.builder.GraphExplorerHttpService;
import com.github.davidmoten.msgraph.builder.internal.MsGraphConstants;

import odata.msgraph.client.beta.container.GraphService;

public final class MsGraph {

    private static final String MSGRAPH_BETA_BASE_URL = "https://graph.microsoft.com/beta";

    private MsGraph() {
        // prevent instantiation
    }

    public static Builder3<GraphService> tenantName(String tenantName) {
        return new MicrosoftClientBuilder<GraphService>(MSGRAPH_BETA_BASE_URL, GraphService::new) //
                .tenantName(tenantName) //
                .resource(MsGraphConstants.RESOURCE_MS_GRAPH) //
                .scope(MsGraphConstants.SCOPE_MS_GRAPH_DEFAULT);
    }

    public static com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.Builder5<GraphService> explorer() {
        return MsGraph //
                .tenantName("unused") //
                .clientId("unused") //
                .clientSecret("unused") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .accessTokenProvider(() -> "{token:https://graph.microsoft.com/}") //
                .httpServiceTransformer(GraphExplorerHttpService::new);
    }
}
