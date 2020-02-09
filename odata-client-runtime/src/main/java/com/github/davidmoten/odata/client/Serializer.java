package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.UnmappedFields;

public final class Serializer {

    public static final Serializer INSTANCE = new Serializer();

    private Serializer() {
        // prevent instantiation
    }

    private static final ObjectMapper MAPPER = createObjectMapper();

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper() //
                .registerModule(new Jdk8Module()) //
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //
                .setSerializationInclusion(Include.NON_NULL) //
        ;
    }

    public <T> T deserialize(String text, Class<? extends T> cls, ContextPath contextPath, boolean addKeysToContextPath) {
        try {
            if (contextPath != null) {
                ObjectMapper m = createObjectMapper();
                Std iv = new InjectableValues.Std() //
                        .addValue(ContextPath.class, contextPath) //
                        .addValue(ChangedFields.class, new ChangedFields()) //
                        .addValue(UnmappedFields.class, new UnmappedFields());
                m.setInjectableValues(iv);
                T t = m.readValue(text, cls);
                if (t instanceof ODataType) {
                    ((ODataType) t).postInject(addKeysToContextPath);
                }
                return t;
            } else {
                return MAPPER.readValue(text, cls);
            }
        } catch (IOException e) {
            System.out.println(text);
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(String text, Class<T> cls) {
        return deserialize(text, cls, null, false);
    }

    public Optional<String> getODataType(String text) {
        try {
            if (text == null) {
                return Optional.empty();
            }
            ObjectNode node = new ObjectMapper().readValue(text, ObjectNode.class);
            return Optional.ofNullable(node.get("@odata.type")).map(JsonNode::asText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends ODataEntityType> String serialize(T entity) {
        ObjectMapper m = createObjectMapper();
        try {
            return m.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends ODataEntityType> String serializeChangesOnly(T entity) {
        try {
            ObjectMapper m = createObjectMapper();
            String s = m.writeValueAsString(entity);
            JsonNode tree = m.readTree(s);
            ObjectNode o = (ObjectNode) tree;
            ChangedFields cf = entity.getChangedFields();
            List<String> list = new ArrayList<>();
            Iterator<String> it = o.fieldNames();
            while (it.hasNext()) {
                String name = it.next();
                if (!cf.contains(name) && !name.equals("@odata.type")) {
                    list.add(name);
                }
            }
            o.remove(list);
            return o.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends ODataEntityType> CollectionPageEntity<T> deserializeCollectionPageEntity(String json,
            Class<T> cls, ContextPath contextPath, SchemaInfo schemaInfo) {
        CollectionInfo<T> c = deserialize(json, cls, contextPath, schemaInfo);
        return new CollectionPageEntity<T>(cls, c.list, c.nextLink, contextPath, schemaInfo);
    }

    public <T> CollectionPageNonEntity<T> deserializeCollectionPageNonEntity(String json, Class<T> cls,
            ContextPath contextPath, SchemaInfo schemaInfo) {
        CollectionInfo<T> c = deserialize(json, cls, contextPath, schemaInfo);
        return new CollectionPageNonEntity<T>(contextPath, cls, c.list, c.nextLink, schemaInfo);
    }

    private <T> CollectionInfo<T> deserialize(String json, Class<T> cls, ContextPath contextPath,
            SchemaInfo schemaInfo) {
        try {
            ObjectMapper m = MAPPER;
            ObjectNode o = m.readValue(json, ObjectNode.class);
            List<T> list = new ArrayList<T>();
            for (JsonNode item : o.get("value")) {
                String text = m.writeValueAsString(item);
                Class<? extends T> subClass = RequestHelper.getSubClass(contextPath, schemaInfo, cls, text);
                list.add(deserialize(text, subClass, contextPath, true));
            }
            // TODO support relative urls using odata.context if present
            Optional<String> nextLink = Optional.ofNullable(o.get("@odata.nextLink")).map(JsonNode::asText);
            return new CollectionInfo<T>(list, nextLink);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class CollectionInfo<T> {

        final List<T> list;
        final Optional<String> nextLink;

        CollectionInfo(List<T> list, Optional<String> nextLink) {
            this.list = list;
            this.nextLink = nextLink;
        }
    }

    public boolean matches(String expectedJson, String actualJson) throws IOException {
        JsonNode expectedTree = MAPPER.readTree(expectedJson);
        JsonNode textTree = MAPPER.readTree(actualJson);
        return expectedTree.equals(textTree);
    }

}
