package com.github.davidmoten.microsoft.authentication;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.RequestHeader;

public final class BearerAuthenticator implements Authenticator {

    private static final String GRAPH_EXPLORER_BASE_URL= "https://proxy.apisandbox.msdn.microsoft.com";
	private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String OAUTH_BEARER_PREFIX = "Bearer ";

    private final Supplier<String> tokenProvider;
    private final String baseUrl;

    public BearerAuthenticator(Supplier<String> tokenProvider, String baseUrl) {
        this.tokenProvider = tokenProvider;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<RequestHeader> authenticate(URL url, List<RequestHeader> m) {
        // chunked upload should not add authorization header hence check on
        // Content-Range
    	String urlString = url.toExternalForm();
        if ( //
                (!urlString.startsWith(baseUrl) //
                 && !urlString.startsWith(GRAPH_EXPLORER_BASE_URL)) //
                || m.stream().anyMatch(x -> x.name().equals(AUTHORIZATION_HEADER_NAME) //
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
