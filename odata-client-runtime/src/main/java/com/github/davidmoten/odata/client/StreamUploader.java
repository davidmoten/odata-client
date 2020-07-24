package com.github.davidmoten.odata.client;

import java.util.concurrent.TimeUnit;

public interface StreamUploader<T extends StreamUploader<T>> {
    
    T requestHeader(String name, String value);

    T connectTimeout(long duration, TimeUnit unit) ;
    
    T readTimeout(long duration, TimeUnit unit); 
}
