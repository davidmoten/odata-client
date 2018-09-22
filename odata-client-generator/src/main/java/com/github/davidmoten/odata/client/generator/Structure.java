package com.github.davidmoten.odata.client.generator;

import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.TProperty;

public interface Structure<T> {

    String getName();

    String getBaseType();

    T value();

    Stream<TProperty> getProperties();
    
}
