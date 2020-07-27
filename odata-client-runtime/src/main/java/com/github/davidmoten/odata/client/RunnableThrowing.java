package com.github.davidmoten.odata.client;

@FunctionalInterface
public interface RunnableThrowing {

    void run() throws Exception;
}
