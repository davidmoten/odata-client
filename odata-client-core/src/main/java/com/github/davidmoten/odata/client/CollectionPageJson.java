package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class CollectionPageJson {

    @JsonProperty("@odata.nextLink")
    private String nextLink;

    @JsonProperty("value")
    private List<JsonNode> values;

    @JsonCreator

    public CollectionPageJson(@JsonProperty("value") List<JsonNode> values,
            @JsonProperty("@odata.nextLink") String nextLink) {
        this.values = values;
        this.nextLink = nextLink;
    }

    public Optional<String> nextLink() {
        return Optional.ofNullable(nextLink);
    }

    public List<JsonNode> values() {
        return values;
    }

}
