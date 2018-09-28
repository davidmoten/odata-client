package com.github.davidmoten.odata.client.generator;

import java.util.List;

public final class Options {

    private final String outputDirectory;

    private final List<SchemaOptions> schemaOptions;

    public Options(String outputDirectory, List<SchemaOptions> schemaOptions) {
        this.outputDirectory = outputDirectory;
        this.schemaOptions = schemaOptions;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public SchemaOptions getSchemaOptions(String namespace) {
        return schemaOptions //
                .stream() //
                .filter(x -> namespace.equals(x.namespace)) //
                .findFirst() //
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException("namespace not found in schemaOptions: " + namespace);
                });
    }

}