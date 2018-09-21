package com.github.davidmoten.odata.client.edm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GeographyPoint {

    @JsonProperty("type")
    private final String type;

    @JsonProperty("coordinates")
    private final double[] coordinates;

    @JsonCreator()
    public GeographyPoint(@JsonProperty("type") String type,
            @JsonProperty("coordinates") double[] coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

}
