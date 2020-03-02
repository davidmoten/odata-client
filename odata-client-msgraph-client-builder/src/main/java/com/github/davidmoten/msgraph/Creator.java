package com.github.davidmoten.msgraph;

import com.github.davidmoten.odata.client.Context;

@FunctionalInterface
public interface Creator<T> {

    T create(Context context);

}
