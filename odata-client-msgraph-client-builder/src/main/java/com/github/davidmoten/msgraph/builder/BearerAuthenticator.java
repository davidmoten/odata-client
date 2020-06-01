package com.github.davidmoten.msgraph.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.RequestHeader;

public final class BearerAuthenticator implements Authenticator {

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String OAUTH_BEARER_PREFIX = "Bearer ";

    private final Supplier<String> tokenProvider;

    public BearerAuthenticator(Supplier<String> tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public List<RequestHeader> authenticate(List<RequestHeader> m) {
        // chunked upload should not add authorization header hence check on
        // Content-Range
        if (m.stream().anyMatch(x -> x.name().equals(AUTHORIZATION_HEADER_NAME)
                || x.name().equals("Content-Range"))) {
            return m;
        } else {
            List<RequestHeader> m2 = new ArrayList<>(m);
            try {
                final String token = tokenProvider.get();
                m2.add(RequestHeader.create(AUTHORIZATION_HEADER_NAME,
                        OAUTH_BEARER_PREFIX + token));
            } catch (Throwable e) {
                throw new ClientException("Unable to authenticate request", e);
            }
            return m2;
        }
    }

}
