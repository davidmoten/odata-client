package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface Paged<T, R extends Paged<T, R>> extends Iterable<T> {

    List<T> values();

    Optional<R> nextPage();

    default List<T> toList() {
        List<T> list = new ArrayList<>();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    default Iterator<T> iterator() {
        return new Iterator<T>() {

            Paged<T, R> page = Paged.this;
            int i = 0;

            @Override
            public boolean hasNext() {
                loadNext();
                return page != null;
            }

            @Override
            public T next() {
                loadNext();
                if (page == null) {
                    throw new NoSuchElementException();
                } else {
                    T v = page.values().get(i);
                    i++;
                    return v;
                }
            }

            private void loadNext() {
                if (page != null) {
                    while (true) {
                        if (page != null && i == page.values().size()) {
                            page = page.nextPage().orElse(null);
                            i = 0;
                        } else {
                            break;
                        }
                    }
                }
            }

        };
    }
}
