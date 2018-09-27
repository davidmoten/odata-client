package org.davidmoten.odata.client.maven;

public class Schema {

    public String namespace;

    public String packageName = "generated";
    public String packageSuffixEnum = ".enums";
    public String packageSuffixEntity = ".entity";
    public String packageSuffixComplexType = ".complex";
    public String packageSuffixEntityRequest = ".entity.request";
    public String packageSuffixCollectionRequest = "collection.request";
    public String packageSuffixContainer = ".container";
    public String packageSuffixSchema = ".schema";
    public String simpleClassNameSchema = "SchemaInfo";
    public String collectionRequestClassSuffix = "CollectionRequest";
    public String entityRequestClassSuffix = "Request";
    public boolean pageComplexTypes = true;

    public String getNamespace() {
        return namespace;
    }

    public String getPackageSuffixEnum() {
        return packageSuffixEnum;
    }

    public void setPackageSuffixEnum(String packageSuffixEnum) {
        this.packageSuffixEnum = packageSuffixEnum;
    }

    public String getPackageSuffixEntity() {
        return packageSuffixEntity;
    }

    public void setPackageSuffixEntity(String packageSuffixEntity) {
        this.packageSuffixEntity = packageSuffixEntity;
    }

    public String getPackageSuffixComplexType() {
        return packageSuffixComplexType;
    }

    public void setPackageSuffixComplexType(String packageSuffixComplexType) {
        this.packageSuffixComplexType = packageSuffixComplexType;
    }

    public String getPackageSuffixEntityRequest() {
        return packageSuffixEntityRequest;
    }

    public void setPackageSuffixEntityRequest(String packageSuffixEntityRequest) {
        this.packageSuffixEntityRequest = packageSuffixEntityRequest;
    }

    public String getPackageSuffixCollectionRequest() {
        return packageSuffixCollectionRequest;
    }

    public void setPackageSuffixCollectionRequest(String packageSuffixCollectionRequest) {
        this.packageSuffixCollectionRequest = packageSuffixCollectionRequest;
    }

    public String getPackageSuffixContainer() {
        return packageSuffixContainer;
    }

    public void setPackageSuffixContainer(String packageSuffixContainer) {
        this.packageSuffixContainer = packageSuffixContainer;
    }

    public String getPackageSuffixSchema() {
        return packageSuffixSchema;
    }

    public void setPackageSuffixSchema(String packageSuffixSchema) {
        this.packageSuffixSchema = packageSuffixSchema;
    }

    public String getSimpleClassNameSchema() {
        return simpleClassNameSchema;
    }

    public void setSimpleClassNameSchema(String simpleClassNameSchema) {
        this.simpleClassNameSchema = simpleClassNameSchema;
    }

    public String getCollectionRequestClassSuffix() {
        return collectionRequestClassSuffix;
    }

    public void setCollectionRequestClassSuffix(String collectionRequestClassSuffix) {
        this.collectionRequestClassSuffix = collectionRequestClassSuffix;
    }

    public String getEntityRequestClassSuffix() {
        return entityRequestClassSuffix;
    }

    public void setEntityRequestClassSuffix(String entityRequestClassSuffix) {
        this.entityRequestClassSuffix = entityRequestClassSuffix;
    }

    public boolean isPageComplexTypes() {
        return pageComplexTypes;
    }

    public void setPageComplexTypes(boolean pageComplexTypes) {
        this.pageComplexTypes = pageComplexTypes;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
