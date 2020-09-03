package com.github.davidmoten.microsoft.authentication;

import java.net.URL;
import java.util.List;

import com.github.davidmoten.odata.client.RequestHeader;

@FunctionalInterface
public interface Authenticator {

    List<RequestHeader> authenticate(URL url, List<RequestHeader> requestHeaders);

}
