package com.github.davidmoten.msgraph.beta;

import odata.msgraph.client.beta.container.GraphService;

public class GraphExplorerMain {
    
    public static void main(String[] args) {
        GraphService client = MsGraph.explorer().build();
        client.applications().forEach(System.out::println);
    }

}
