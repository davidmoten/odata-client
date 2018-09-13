package com.github.davidmoten.odata.client;

public interface Service {

    public ResponseGet getResponseGET(String url);

    public Path getBasePath();

}
