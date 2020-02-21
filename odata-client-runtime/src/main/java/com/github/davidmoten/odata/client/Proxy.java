package com.github.davidmoten.odata.client;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;

public class Proxy {
    
    
    public static void main(String[] args) {
        String host = "";
        int port = 0;
        String scheme = "https";
        HttpHost proxy = new HttpHost(host, port, scheme);
        RequestConfig.custom().setProxy(proxy);
    }
}
