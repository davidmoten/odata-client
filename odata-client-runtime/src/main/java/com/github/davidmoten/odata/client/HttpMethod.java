package com.github.davidmoten.odata.client;

import java.util.Collections;
import java.util.Set;

import com.github.davidmoten.guavamini.Sets;

public enum HttpMethod {
    GET, PATCH, POST, PUT, DELETE;

    private static final Set<HttpMethod> createOrUpdateMethods = Collections
            .unmodifiableSet(Sets.newHashSet(PATCH, POST, PUT));

    public static final Set<HttpMethod> createOrUpdateMethods() {
        return createOrUpdateMethods;
    }

}
