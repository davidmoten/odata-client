package com.github.davidmoten.odata.client;

@FunctionalInterface
public interface UploadListener {
    
    void bytesWritten(long count);
    
    UploadListener IGNORE = count -> {
        // do nothing
    };
   
}
