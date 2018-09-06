package com.github.davidmoten.odata.client;

public class Options {
    
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

}
