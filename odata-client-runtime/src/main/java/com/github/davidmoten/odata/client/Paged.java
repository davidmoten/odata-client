package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Paged<T, R extends Paged<T, R>> extends Iterable<T> {

    List<T> currentPage();

    Optional<R> nextPage();

    default List<T> toList() {
        return Util.add(this, new ArrayList<T>());
    }
    
    default Set<T> toSet() {
        return Util.add(this, new HashSet<T>());
    }

    default Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
    
    default <S> S to(Function<? super Paged<T, R>,? extends S> function) {
    	return function.apply(this);
    }

    @Override
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
                    T v = page.currentPage().get(i);
                    i++;
                    return v;
                }
            }

            private void loadNext() {
                while (true) {
                    if (page != null && i == page.currentPage().size()) {
                        page = page.nextPage().orElse(null);
                        i = 0;
                    } else {
                        break;
                    }
                }
            }

        };
    }
}
