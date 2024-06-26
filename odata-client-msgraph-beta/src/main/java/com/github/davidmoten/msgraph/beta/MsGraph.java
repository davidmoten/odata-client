package com.github.davidmoten.msgraph.beta;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.microsoft.authentication.GraphConstants;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.BuilderWithScopes;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.BuilderWithBasicAuthentication;
import com.github.davidmoten.msgraph.builder.GraphExplorerHttpService;

import odata.msgraph.client.beta.container.GraphService;
import odata.msgraph.client.beta.schema.SchemaInfo;

public final class MsGraph {

    private static final String MSGRAPH_BETA_BASE_URL = "https://graph.microsoft.com/beta";

    private MsGraph() {
        // prevent instantiation
    }

    public static BuilderWithScopes<GraphService> tenantName(String tenantName) {
        return MicrosoftClientBuilder//
                .baseUrl(MSGRAPH_BETA_BASE_URL) //
                .creator(GraphService::new) //
                .addSchema(SchemaInfo.INSTANCE) //
                .addSchema(odata.msgraph.client.beta.callRecords.schema.SchemaInfo.INSTANCE) //
                .addSchema(odata.msgraph.client.beta.termStore.schema.SchemaInfo.INSTANCE) //
                .build() //
                .tenantName(tenantName) //
                .resource(GraphConstants.RESOURCE_MS_GRAPH) //
                .scope(GraphConstants.SCOPE_MS_GRAPH_DEFAULT);
    }

    public static BuilderWithBasicAuthentication<GraphService> explorer() {
        return MsGraph //
                .tenantName("unused") //
                .clientId("unused") //
                .clientSecret("unused") //
                .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
                .accessTokenProvider(() -> "{token:https://graph.microsoft.com/}") //
                .httpServiceTransformer(GraphExplorerHttpService::new);
    }
}
