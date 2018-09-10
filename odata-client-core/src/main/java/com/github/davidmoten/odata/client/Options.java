package com.github.davidmoten.odata.client;

public final class Options {

    public String outputDirectory() {
        return "target/generated-sources/odata";
    }

    public String pkg() {
        return "microsoft.graph.generated";
    }

    public String packageSuffixEnum() {
        return ".enums";
    }

    public String packageSuffixEntity() {
        return ".entity";
    }

    public String packageSuffixComplexType() {
        return ".complex";
    }

    public String packageSuffixCollectionRequest() {
        return ".collection.request";
    }

    public String packageSuffixEntityRequest() {
        return ".entity.request";
    }

    public String collectionRequestClassSuffix() {
        return "CollectionRequest";
    }

    public String packageSuffixContainer() {
        return ".container";
    }

    public String entityRequestClassSuffix() {
        return "Request";
    }

}
