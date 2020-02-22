package com.github.davidmoten.msgraph.beta;

import com.github.davidmoten.msgraph.MsGraphClientBuilder;

import odata.msgraph.client.container.GraphService;

public final class MsGraph {

    private static final String MSGRAPH_BETA_BASE_URL = "https://graph.microsoft.com/beta";

    private MsGraph() {
        // prevent instantiation
    }

    public static MsGraphClientBuilder<GraphService> tenantName(String tenantName) {
        return new MsGraphClientBuilder<GraphService>(MSGRAPH_BETA_BASE_URL, tenantName, context -> new GraphService(context));
    }
}
