package com.github.davidmoten.odata.client;

import java.util.Optional;

public final class ClientException extends RuntimeException {

    private static final long serialVersionUID = -2373424382425163041L;
    
    private final Optional<Integer> statusCode;

    public ClientException(int statusCode, String message) {
        super(message);
        this.statusCode = Optional.of(statusCode);
    }
    
    public ClientException(String message) {
        super(message);
        this.statusCode = Optional.empty();
    }

    public ClientException(Throwable e) {
        super(e);
        this.statusCode = Optional.empty();
    }

    public ClientException(int statusCode, Throwable e) {
        super(e);
        this.statusCode = Optional.of(statusCode);
    }
    
    public ClientException(String message, Throwable e) {
        super(message, e);
        this.statusCode = Optional.empty();
    }

    public Optional<Integer> getStatusCode() {
        return statusCode;
    }
    
    /**
     * If e is a ClientException then returns e otherwise returns e wrapped in a ClientException.
     * @param e throwable to return as ClientException
     * @return client exception
     */
    public static ClientException from(Throwable e) {
        if (e instanceof ClientException) {
            return (ClientException) e;
        } else {
            return new ClientException(e);
        }
    }
}
