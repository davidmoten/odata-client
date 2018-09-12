package com.github.davidmoten.odata.client;

import java.util.List;

public interface Service {

    public ResponseGet getResponseGET(List<PathItem> path);
    
    public ResponseGet getResponseGETRelative(List<PathItem> path);

}
