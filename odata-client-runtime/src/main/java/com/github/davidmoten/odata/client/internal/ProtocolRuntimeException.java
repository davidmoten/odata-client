package com.github.davidmoten.odata.client.internal;

import java.net.ProtocolException;

public final class ProtocolRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1694385634304829978L;

    public ProtocolRuntimeException(ProtocolException e) {
        super(e);
    }

}
