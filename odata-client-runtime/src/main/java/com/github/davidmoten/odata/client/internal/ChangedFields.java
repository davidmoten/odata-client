package com.github.davidmoten.odata.client.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Immutable
 */
public final class ChangedFields implements Iterable<String> {

    private final Set<String> set;
    
    public static final ChangedFields EMPTY = new ChangedFields();

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

            final Iterator<String> it = set.iterator();

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

    public boolean contains(String name) {
        return set.contains(name);
    }

    public ChangedFields add(String name) {
        Set<String> set2 = new HashSet<String>(set);
        set2.add(name);
        return new ChangedFields(set2);
    }

    public Set<String> toSet() {
        return StreamSupport.stream(this.spliterator(), false).collect(Collectors.toSet());
    }

}
