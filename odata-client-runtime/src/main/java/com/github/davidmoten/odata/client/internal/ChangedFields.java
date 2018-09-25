package com.github.davidmoten.odata.client.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ChangedFields implements Iterable<String> {

    public static final ChangedFields EMPTY = new ChangedFields();

    private final Set<String> set;

    private ChangedFields(Set<String> set) {
        this.set = set;
    }

    public ChangedFields() {
        this(Collections.emptySet());
    }

    @Override
    public Iterator<String> iterator() {
        // return a custom iterator that does not allow removal
        return new Iterator<String>() {

            Iterator<String> it = set.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public String next() {
                return it.next();
            }
        };
    }

    public ChangedFields add(String name) {
        Set<String> set2 = new HashSet<String>(set);
        set2.add(name);
        return new ChangedFields(set2);
    }

}
