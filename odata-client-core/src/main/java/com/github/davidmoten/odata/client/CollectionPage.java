package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

public interface CollectionPage<T extends ODataEntity> {

    List<T> currentPage();
    
    Optional<CollectionPage<T>> nextPage();
    
}
