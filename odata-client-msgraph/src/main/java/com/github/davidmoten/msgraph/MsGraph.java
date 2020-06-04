package com.github.davidmoten.msgraph;

import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;

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
}
