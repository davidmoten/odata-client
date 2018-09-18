package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CollectionPageEntity<T extends ODataEntity> {

    private final Class<T> cls;
    private final List<T> list;
    private final Optional<String> nextLink;
    private final ContextPath contextPath;

    public CollectionPageEntity(Class<T> cls, List<T> list, Optional<String> nextLink, ContextPath contextPath) {
        this.cls = cls;
        this.list = list;
        this.nextLink = nextLink;
        this.contextPath = contextPath;
    }

    public List<T> currentPage() {
        return list;
    }

    public Optional<CollectionPageEntity<T>> nextPage() {
        if (nextLink.isPresent()) {
            // TODO add request headers used in initial call?
            ResponseGet response = contextPath.context().service().GET(nextLink.get(), Collections.emptyMap());
            // odata 4 says the "value" element of the returned json is an array of
            // serialized T see example at
            // https://www.odata.org/getting-started/basic-tutorial/#entitySet
            return Optional.of(create(response.getText(), cls, contextPath));
        } else {
            return Optional.empty();
        }
    }

    public static <T extends ODataEntity> CollectionPageEntity<T> create(String json, Class<T> cls,
            ContextPath contextPath) {
        try {
            ObjectMapper m = Serialization.MAPPER;
            ObjectNode o = m.readValue(json, ObjectNode.class);
            List<T> list2 = new ArrayList<T>();
            for (JsonNode item : o.get("value")) {
                list2.add(contextPath.context().serializer().deserialize(m.writeValueAsString(item), cls, contextPath));
            }
            Optional<String> nextLink2 = Optional.ofNullable(o.get("@odata.nextLink")).map(JsonNode::asText);
            return new CollectionPageEntity<T>(cls, list2, nextLink2, contextPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends ODataEntity> CollectionPageEntity<T> from(Context context, CollectionPageJson c,
            Class<T> cls) {
        // TODO
        return null;
    }

}
