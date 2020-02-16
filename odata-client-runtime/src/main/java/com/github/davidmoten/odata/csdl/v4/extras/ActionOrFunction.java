package com.github.davidmoten.odata.csdl.v4.extras;

public interface ActionOrFunction {
    String getName();
    String getEntitySetPath();
    boolean isIsBound();
}
