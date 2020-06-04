package com.github.davidmoten.msgraph.builder;

import com.github.davidmoten.guavamini.Preconditions;

public enum AuthenticationEndpoint {

    GLOBAL("https://login.microsoftonline.com/"), //
    GLOBAL2("https://login.windows.net/"), //
    CHINA("https://login.chinacloudapi.cn/"), //
    GERMANY("https://login.microsoftonline.de/"), //
    US_GOVERNMENT("https://login.microsoftonline.us/");

    private String url;

    private AuthenticationEndpoint(String url) {
        Preconditions.checkArgument(url.endsWith("/"));
        this.url = url;
    }

    public String url() {
        return url;
    }

}
