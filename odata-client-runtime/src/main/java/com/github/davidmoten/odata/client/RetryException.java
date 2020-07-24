package com.github.davidmoten.odata.client;

public final class RetryException extends RuntimeException {

    private static final long serialVersionUID = -8485628454671949441L;

    public RetryException(String message, Throwable t) {
        super(message, t);
    }

    public RetryException(String message) {
        super(message);
    }

}
