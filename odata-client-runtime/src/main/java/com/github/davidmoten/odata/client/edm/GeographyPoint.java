package com.github.davidmoten.odata.client.edm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GeographyPoint {

    @JsonProperty("values")
    private final double[] values;

    @JsonCreator()
    public GeographyPoint(@JsonProperty("values") double[] values) {
        this.values = values;
    }

    public double[] values() {
        return values;
    }

}
