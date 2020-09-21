package com.github.davidmoten.odata.client;

public interface HasContext {

    Context _context();

    default CustomRequest _custom() {
        return new CustomRequest(_context(), false);
    }

}
