package com.github.davidmoten.msgraph.builder;

import com.github.davidmoten.odata.client.Context;

@FunctionalInterface
public interface Creator<T> {

    T create(Context context);

}
