package com.github.davidmoten.odata.client.generator;

import java.util.Optional;


public final class Options {

    private final String outputDirectory;
    private final String pkg;
    private final String packageSuffixEnum;
    private final String packageSuffixEntity;
    private final String packageSuffixComplexType;
    private final String packageSuffixEntityRequest;
    private final String packageSuffixCollectionRequest;
    private final String packageSuffixContainer;
    private final String collectionRequestClassSuffix;
    private final String entityRequestClassSuffix;
    private final boolean pageComplexTypes;

    private Options(String outputDirectory, String pkg, String packageSuffixEnum, String packageSuffixEntity, String packageSuffixComplexType, String packageSuffixEntityRequest, String packageSuffixCollectionRequest, String packageSuffixContainer, String collectionRequestClassSuffix, String entityRequestClassSuffix, boolean pageComplexTypes) {
        notNull(outputDirectory, "outputDirectory");
        notNull(pkg, "pkg");
        notNull(packageSuffixEnum, "packageSuffixEnum");
        notNull(packageSuffixEntity, "packageSuffixEntity");
        notNull(packageSuffixComplexType, "packageSuffixComplexType");
        notNull(packageSuffixEntityRequest, "packageSuffixEntityRequest");
        notNull(packageSuffixCollectionRequest, "packageSuffixCollectionRequest");
        notNull(packageSuffixContainer, "packageSuffixContainer");
        notNull(collectionRequestClassSuffix, "collectionRequestClassSuffix");
        notNull(entityRequestClassSuffix, "entityRequestClassSuffix");
        notNull(pageComplexTypes, "pageComplexTypes");
        this.outputDirectory = outputDirectory;
        this.pkg = pkg;
        this.packageSuffixEnum = packageSuffixEnum;
        this.packageSuffixEntity = packageSuffixEntity;
        this.packageSuffixComplexType = packageSuffixComplexType;
        this.packageSuffixEntityRequest = packageSuffixEntityRequest;
        this.packageSuffixCollectionRequest = packageSuffixCollectionRequest;
        this.packageSuffixContainer = packageSuffixContainer;
        this.collectionRequestClassSuffix = collectionRequestClassSuffix;
        this.entityRequestClassSuffix = entityRequestClassSuffix;
        this.pageComplexTypes = pageComplexTypes;
    }

    public static Builder1 builder() {
        return new Builder1();
    }

    public String outputDirectory() {
        return outputDirectory;
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

    public String collectionRequestClassSuffix() {
        return collectionRequestClassSuffix;
    }

    public String entityRequestClassSuffix() {
        return entityRequestClassSuffix;
    }

    public boolean pageComplexTypes() {
        return pageComplexTypes;
    }

    public static final class Builder1 {

        private String outputDirectory = "target/generated-sources/odata";
        private String pkg;
        private String packageSuffixEnum = ".enums";
        private String packageSuffixEntity = ".entity";
        private String packageSuffixComplexType = ".complex";
        private String packageSuffixEntityRequest = ".entity.request";
        private String packageSuffixCollectionRequest = ".collection.request";
        private String packageSuffixContainer = ".container";
        private String collectionRequestClassSuffix = "CollectionRequest";
        private String entityRequestClassSuffix = "Request";
        private boolean pageComplexTypes = true;

        Builder1(){
        }

        public Builder2 pkg(String pkg) {
            notNull(pkg, "pkg");
            this.pkg = pkg;
            return new Builder2(this);
        }

    }

    public static final class Builder2 {

        private final Builder1 b;

        Builder2(Builder1 b) {
             this.b = b;
        }

        public Builder2 outputDirectory(String outputDirectory) {
            notNull(outputDirectory, "outputDirectory");
            b.outputDirectory = outputDirectory;
            return this;
        }

        public Builder2 packageSuffixEnum(String packageSuffixEnum) {
            notNull(packageSuffixEnum, "packageSuffixEnum");
            b.packageSuffixEnum = packageSuffixEnum;
            return this;
        }

        public Builder2 packageSuffixEntity(String packageSuffixEntity) {
            notNull(packageSuffixEntity, "packageSuffixEntity");
            b.packageSuffixEntity = packageSuffixEntity;
            return this;
        }

        public Builder2 packageSuffixComplexType(String packageSuffixComplexType) {
            notNull(packageSuffixComplexType, "packageSuffixComplexType");
            b.packageSuffixComplexType = packageSuffixComplexType;
            return this;
        }

        public Builder2 packageSuffixEntityRequest(String packageSuffixEntityRequest) {
            notNull(packageSuffixEntityRequest, "packageSuffixEntityRequest");
            b.packageSuffixEntityRequest = packageSuffixEntityRequest;
            return this;
        }

        public Builder2 packageSuffixCollectionRequest(String packageSuffixCollectionRequest) {
            notNull(packageSuffixCollectionRequest, "packageSuffixCollectionRequest");
            b.packageSuffixCollectionRequest = packageSuffixCollectionRequest;
            return this;
        }

        public Builder2 packageSuffixContainer(String packageSuffixContainer) {
            notNull(packageSuffixContainer, "packageSuffixContainer");
            b.packageSuffixContainer = packageSuffixContainer;
            return this;
        }

        public Builder2 collectionRequestClassSuffix(String collectionRequestClassSuffix) {
            notNull(collectionRequestClassSuffix, "collectionRequestClassSuffix");
            b.collectionRequestClassSuffix = collectionRequestClassSuffix;
            return this;
        }

        public Builder2 entityRequestClassSuffix(String entityRequestClassSuffix) {
            notNull(entityRequestClassSuffix, "entityRequestClassSuffix");
            b.entityRequestClassSuffix = entityRequestClassSuffix;
            return this;
        }

        public Builder2 pageComplexTypes(boolean pageComplexTypes) {
            notNull(pageComplexTypes, "pageComplexTypes");
            b.pageComplexTypes = pageComplexTypes;
            return this;
        }

        public Options build() {
            return new Options(b.outputDirectory, b.pkg, b.packageSuffixEnum, b.packageSuffixEntity, b.packageSuffixComplexType, b.packageSuffixEntityRequest, b.packageSuffixCollectionRequest, b.packageSuffixContainer, b.collectionRequestClassSuffix, b.entityRequestClassSuffix, b.pageComplexTypes);
        }
    }

    private static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name + " cannot be null");
        }
    }
}