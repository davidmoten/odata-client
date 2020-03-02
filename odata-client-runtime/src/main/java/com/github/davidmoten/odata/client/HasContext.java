package com.github.davidmoten.odata.client;

public interface HasContext {

    public Context _context();

    public default CustomRequest _custom() {
        return new CustomRequest(_context());
    }

}
