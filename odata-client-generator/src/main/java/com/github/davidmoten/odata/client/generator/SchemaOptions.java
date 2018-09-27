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
    public final boolean pageComplexTypes;

    public SchemaOptions(String namespace, String pkg, String packageSuffixEnum, String packageSuffixEntity,
            String packageSuffixComplexType, String packageSuffixEntityRequest, String packageSuffixCollectionRequest,
            String packageSuffixContainer, String packageSuffixSchema, String simpleClassNameSchema,
            String collectionRequestClassSuffix, String entityRequestClassSuffix, boolean pageComplexTypes) {
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
        this.pageComplexTypes = pageComplexTypes;
    }

    public SchemaOptions(String namespace, String pkg) {
        this.namespace = namespace;
        this.pkg = pkg;
        this.packageSuffixEnum = ".enums";
        this.packageSuffixEntity = ".entity";
        this.packageSuffixComplexType = ".complex";
        this.packageSuffixEntityRequest = ".entity.request";
        this.packageSuffixCollectionRequest = ".collection.request";
        this.packageSuffixContainer = ".container";
        this.packageSuffixSchema = ".schema";
        this.simpleClassNameSchema = "SchemaInfo";
        this.collectionRequestClassSuffix = "CollectionRequest";
        this.entityRequestClassSuffix = "EntityRequest";
        this.pageComplexTypes = true;
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

    public String packageSuffixCollectionRequest() {
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

}
