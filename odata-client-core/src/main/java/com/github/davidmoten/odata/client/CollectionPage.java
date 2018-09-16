package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public class CollectionPage<T> {

    private final Class<T> cls;
    private final List<T> list;
    private final Optional<String> nextLink;
    private final Context context;

    public CollectionPage(Class<T> cls, List<T> list, Optional<String> nextLink, Context context) {
        this.cls = cls;
        this.list = list;
        this.nextLink = nextLink;
        this.context = context;
    }

    public List<T> currentPage() {
        return list;
    }

    public Optional<CollectionPage<T>> nextPage() {
        if (nextLink.isPresent()) {
            // TODO add request headers used in initial call?
            ResponseGet response = context.service().getResponseGET(nextLink.get(), Collections.emptyMap());
            // odata 4 says the "value" element of the returned json is an array of
            // serialized T see example at
            // https://www.odata.org/getting-started/basic-tutorial/#entitySet
            return nextPage(response.getText(), cls, context);
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static <T> Optional<CollectionPage<T>> nextPage(String json, Class<T> cls, Context context) {
        try {
            ObjectMapper m = Serialization.MAPPER;
            ObjectNode o = m.readValue(json, ObjectNode.class);
            List<T> list2 = new ArrayList<T>();
            for (JsonNode item : o.get("value")) {
                list2.add(context.serializer().deserialize(m.writeValueAsString(item), cls));
            }
            Optional<String> nextLink2 = Optional.ofNullable(o.get("@odata.nextLink").asText());
            return Optional.ofNullable(new CollectionPage<T>(cls, list2, nextLink2, context));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> CollectionPage<T> from(Context context, CollectionPageJson c, Class<T> cls) {
        //TODO
        return null;
    }

}
