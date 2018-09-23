package com.github.davidmoten.odata.client.generator;

import java.util.List;

import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

public interface Structure<T> {

    String getName();

    String getBaseType();

    T value();

    List<TProperty> getProperties();

    List<TNavigationProperty> getNavigationProperties();

    List<T> getHeirarchy();

}
