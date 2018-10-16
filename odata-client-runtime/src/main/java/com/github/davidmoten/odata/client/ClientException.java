package com.github.davidmoten.odata.client;

public final class ClientException extends RuntimeException {

    private static final long serialVersionUID = -2373424382425163041L;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(Throwable e) {
        super(e);
    }
}
