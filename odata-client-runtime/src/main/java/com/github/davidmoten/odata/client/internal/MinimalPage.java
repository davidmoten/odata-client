package com.github.davidmoten.odata.client.internal;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"@odata.nextLink","value"})
public class MinimalPage<T> {

    @JsonProperty("value")
    final List<T> list;
    
    @JsonProperty("@odata.nextLink")
    final String nextLink;
    
    public MinimalPage(List<T> list, Optional<String> nextLink) {
        this.list = list;
        this.nextLink = nextLink.orElse(null);
    }
}
