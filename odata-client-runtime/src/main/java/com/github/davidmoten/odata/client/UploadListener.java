package com.github.davidmoten.odata.client;

@FunctionalInterface
public interface UploadListener {
    
    void bytesWritten(long count);
    
    public static final UploadListener IGNORE = new UploadListener() {

        @Override
        public void bytesWritten(long count) {
            // do nothing
        }
        
    };
   
}
