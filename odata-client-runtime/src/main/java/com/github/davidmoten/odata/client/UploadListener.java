package com.github.davidmoten.odata.client;

@FunctionalInterface
public interface UploadListener {
    
    void bytesWritten(long count);
    
    UploadListener IGNORE = new UploadListener() {

        @Override
        public void bytesWritten(long count) {
            // do nothing
        }
        
    };
   
}
