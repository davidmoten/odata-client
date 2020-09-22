package org.davidmoten.odata.client.maven;

public class Schema {

    public String namespace;
    public String packageName = "generated";
    public String packageSuffixEnum = ".enums";
    public String packageSuffixEntity = ".entity";
    public String packageSuffixComplexType = ".complex";
    public String packageSuffixEntityRequest = ".entity.request";
    public String packageSuffixCollectionRequest = ".entity.collection.request";
    public String packageSuffixContainer = ".container";
    public String packageSuffixSchema = ".schema";
    public String simpleClassNameSchema = "SchemaInfo";
    public String collectionRequestClassSuffix = "CollectionRequest";
    public String entityRequestClassSuffix = "Request";
    public String actionRequestClassSuffix = "ActionRequest";
    public boolean pageComplexTypes = true;
    public boolean failOnMissingEntitySet = true;

}
