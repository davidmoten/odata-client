package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CollectionPageImpl<T extends ODataEntity> implements CollectionPage<T> {

    private final Class<T> cls;
    private final List<T> list;
    private final Optional<String> nextLink;
    private final Context context;

    public CollectionPageImpl(Class<T> cls, List<T> list, Optional<String> nextLink, Context context) {
        this.cls = cls;
        this.list = list;
        this.nextLink = nextLink;
        this.context = context;
    }

    @Override
    public List<T> currentPage() {
        return list;
    }

    @Override
    public Optional<CollectionPage<T>> nextPage() {
        if (nextLink.isPresent()) {
            ResponseGet response = context.service().getResponseGET(nextLink.get());
            // odata 4 says the "value" element of the returned json is an array of
            // serialized T
            // see example at
            // https://www.odata.org/getting-started/basic-tutorial/#entitySet
            ObjectMapper m = Serialization.MAPPER;
            try {
                ObjectNode o = m.readValue(response.getJson(), ObjectNode.class);
                List<T> list2 = new ArrayList<T>();
                for (JsonNode item : o.get("value")) {
                    list2.add(context.serializer().deserialize(m.writeValueAsString(item), cls));
                }
                Optional<String> nextLink2 = Optional.ofNullable(o.get("@odata.nextLink").asText());
                return Optional.ofNullable(new CollectionPageImpl<T>(cls, list2, nextLink2, context));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

}
