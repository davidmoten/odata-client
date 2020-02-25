package com.github.davidmoten.msgraph;

import com.github.davidmoten.guavamini.Preconditions;

public enum GraphEndpoint {

    GLOBAL("https://login.microsoftonline.com/"), //
    GLOBAL2("https://login.windows.net/"), //
    CHINA("https://login.chinacloudapi.cn/"), //
    GERMANY("https://login.microsoftonline.de/"), //
    US_GOVERNMENT("https://login.microsoftonline.us/");

    private String url;

    private GraphEndpoint(String url) {
        Preconditions.checkArgument(url.endsWith("/"));
        this.url = url;
    }

    public String url() {
        return url;
    }

}
