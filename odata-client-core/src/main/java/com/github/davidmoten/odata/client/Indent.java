package com.github.davidmoten.odata.client;

public class Indent {

    private String value = ""; 
    
    public Indent left() {
        value = value.substring(0, value.length() - 4);
        return this;
    }
    
    public Indent right() {
        value +="    ";
        return this;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
}
