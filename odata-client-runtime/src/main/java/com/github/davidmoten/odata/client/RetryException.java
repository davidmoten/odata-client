package com.github.davidmoten.odata.client;

public final class RetryException extends RuntimeException {

    public RetryException(String message, Throwable t) {
        super(message, t);
    }

    public RetryException(String message) {
        super(message);
    }

}
