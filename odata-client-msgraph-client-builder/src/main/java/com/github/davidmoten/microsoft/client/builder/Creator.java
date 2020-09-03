package com.github.davidmoten.microsoft.client.builder;

import com.github.davidmoten.odata.client.Context;

@FunctionalInterface
public interface Creator<T> {

    T create(Context context);

}
