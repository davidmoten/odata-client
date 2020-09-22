package com.github.davidmoten.odata.client.generator;

public class SchemaOptions {
    public final String namespace;
    public final String pkg;
    public final String packageSuffixEnum;
    public final String packageSuffixEntity;
    public final String packageSuffixComplexType;
    public final String packageSuffixEntityRequest;
    public final String packageSuffixCollectionRequest;
    public final String packageSuffixContainer;
    public final String packageSuffixSchema;
    public final String simpleClassNameSchema;
    public final String collectionRequestClassSuffix;
    public final String entityRequestClassSuffix;

    public final String actionRequestClassSuffix;
    public final boolean pageComplexTypes;
    public final boolean failOnMissingEntitySet;
    
    // TODO make configurable
    private final String packageSuffixComplexTypeCollectionRequest = ".complex.collection.request";

    public SchemaOptions(String namespace, String pkg, String packageSuffixEnum,
            String packageSuffixEntity, String packageSuffixComplexType,
            String packageSuffixEntityRequest, String packageSuffixCollectionRequest,
            String packageSuffixContainer,
            String packageSuffixSchema, String simpleClassNameSchema,
            String collectionRequestClassSuffix, String entityRequestClassSuffix,
            String actionRequestClassSuffix, boolean pageComplexTypes, boolean failOnMissingEntitySet) {
        this.namespace = namespace;
        this.pkg = pkg;
        this.packageSuffixEnum = packageSuffixEnum;
        this.packageSuffixEntity = packageSuffixEntity;
        this.packageSuffixComplexType = packageSuffixComplexType;
        this.packageSuffixEntityRequest = packageSuffixEntityRequest;
        this.packageSuffixCollectionRequest = packageSuffixCollectionRequest;
        this.packageSuffixContainer = packageSuffixContainer;
        this.packageSuffixSchema = packageSuffixSchema;
        this.simpleClassNameSchema = simpleClassNameSchema;
        this.collectionRequestClassSuffix = collectionRequestClassSuffix;
        this.entityRequestClassSuffix = entityRequestClassSuffix;
        this.actionRequestClassSuffix = actionRequestClassSuffix;
        this.pageComplexTypes = pageComplexTypes;
        this.failOnMissingEntitySet = failOnMissingEntitySet;
    }

    public SchemaOptions(String namespace, String pkg) {
        this(namespace, pkg, ".enums", ".entity", ".complex", ".entity.request", ".collection.request",
                ".container", ".schema", "SchemaInfo", "CollectionRequest", "EntityRequest", "ActionRequest", true, true);
    }

    public String pkg() {
        return pkg;
    }

    public String packageSuffixEnum() {
        return packageSuffixEnum;
    }

    public String packageSuffixEntity() {
        return packageSuffixEntity;
    }

    public String packageSuffixComplexType() {
        return packageSuffixComplexType;
    }

    public String packageSuffixEntityRequest() {
        return packageSuffixEntityRequest;
    }

    public String packageSuffixEntityCollectionRequest() {
        return packageSuffixCollectionRequest;
    }

    public String packageSuffixContainer() {
        return packageSuffixContainer;
    }

    public String packageSuffixSchema() {
        return packageSuffixSchema;
    }

    public String simpleClassNameSchema() {
        return simpleClassNameSchema;
    }

    public String collectionRequestClassSuffix() {
        return collectionRequestClassSuffix;
    }

    public String entityRequestClassSuffix() {
        return entityRequestClassSuffix;
    }

    public boolean pageComplexTypes() {
        return pageComplexTypes;
    }

    public String packageSuffixComplexTypeCollectionRequest() {
        return packageSuffixComplexTypeCollectionRequest;
    }

    public String packageSuffixEntitySet() {
        // TODO make configurable
        return packageSuffixEntity() + ".set";
    }

}
