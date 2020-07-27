package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.UnmappedFields;

public final class Serializer {

    // must instantiate this before MAPPER_*
    private static final JacksonAnnotationIntrospector IGNORE_JSON_INCLUDE_ANNOTATION = new JacksonAnnotationIntrospector() {

        private static final long serialVersionUID = 4940526677740939988L;

        @Override
        protected <A extends Annotation> A _findAnnotation(final Annotated annotated,
                final Class<A> annoClass) {
            if (!annotated.hasAnnotation(JsonInclude.class)) {
                return super._findAnnotation(annotated, annoClass);
            } else {
                return null;
            }
        }
    };
    
    private static final ObjectMapper MAPPER_EXCLUDE_NULLS = createObjectMapper(false);
    private static final ObjectMapper MAPPER_INCLUDE_NULLS = createObjectMapper(true);

    public static final Serializer INSTANCE = new Serializer();

    private Serializer() {
        // prevent instantiation
    }
    
    @VisibleForTesting
    static ObjectMapper createObjectMapper(boolean includeNulls) {
        return new ObjectMapper() //
                .setAnnotationIntrospector(IGNORE_JSON_INCLUDE_ANNOTATION) //
                .registerModule(new Jdk8Module()) //
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //
                .setSerializationInclusion(includeNulls ? Include.ALWAYS : Include.NON_NULL) //
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
                
                // StdDateFormat is ISO8601 since jackson 2.9
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    public <T> T deserialize(String text, Class<? extends T> cls, ContextPath contextPath,
            boolean addKeysToContextPath) {
        try {
            if (contextPath != null) {
                ObjectMapper m = MAPPER_EXCLUDE_NULLS.copy();
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
                return MAPPER_EXCLUDE_NULLS.readValue(text, cls);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T, S> T deserializeWithParametricType(String text, Class<? extends T> cls,
            Class<? extends S> parametricTypeClass, ContextPath contextPath,
            boolean addKeysToContextPath) {
        try {
            ObjectMapper m = MAPPER_EXCLUDE_NULLS.copy();
            JavaType type = m.getTypeFactory().constructParametricType(cls,
                    parametricTypeClass);
            if (contextPath != null) {
                Std iv = new InjectableValues.Std() //
                        .addValue(ContextPath.class, contextPath) //
                        .addValue(ChangedFields.class, new ChangedFields()) //
                        .addValue(UnmappedFields.class, new UnmappedFields());
                m.setInjectableValues(iv);
                T t = m.readValue(text, type);
                if (t instanceof ODataType) {
                    ((ODataType) t).postInject(addKeysToContextPath);
                }
                return t;
            } else {
                return m.readValue(text, type);
            }
        } catch (IOException e) {
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
            return Optional.empty();
        }
    }

    public String serialize(Object entity) {
        try {
            return MAPPER_EXCLUDE_NULLS.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String serializePrettyPrint(Object entity) {
        try {
            return MAPPER_EXCLUDE_NULLS.writerWithDefaultPrettyPrinter().writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends ODataEntityType> String serializeChangesOnlyPrettyPrint(T entity) {
        return serializeChangesOnly(entity, true);
    }
    
    public <T extends ODataEntityType> String serializeChangesOnly(T entity) {
        return serializeChangesOnly(entity, false);
    }
    
    public <T extends ODataEntityType> String serializeChangesOnly(T entity, boolean prettyPrint) {
        try {
            ObjectMapper m = MAPPER_INCLUDE_NULLS;
            final String s;
            if (prettyPrint) {
                s = m.writerWithDefaultPrettyPrinter().writeValueAsString(entity);    
            } else {
                s = m.writeValueAsString(entity);
            }
            
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
            throw new UncheckedIOException(e);
        }
    }

    public <T> CollectionPage<T> deserializeCollectionPage(String json, Class<T> cls,
            ContextPath contextPath, SchemaInfo schemaInfo, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
        CollectionInfo<T> c = deserializeToCollection(json, cls, contextPath, schemaInfo);
        return new CollectionPage<T>(contextPath, cls, c.list, c.nextLink, schemaInfo,
                requestHeaders, options);
    }

    private <T> CollectionInfo<T> deserializeToCollection(String json, Class<T> cls,
            ContextPath contextPath, SchemaInfo schemaInfo) {
        try {
            ObjectMapper m = MAPPER_EXCLUDE_NULLS;
            ObjectNode o = m.readValue(json, ObjectNode.class);
            List<T> list = new ArrayList<T>();
            for (JsonNode item : o.get("value")) {
                String text = m.writeValueAsString(item);
                Class<? extends T> subClass = RequestHelper.getSubClass(contextPath, schemaInfo,
                        cls, text);
                list.add(deserialize(text, subClass, contextPath, true));
            }
            // TODO support relative urls using odata.context if present
            Optional<String> nextLink = Optional.ofNullable(o.get("@odata.nextLink"))
                    .map(JsonNode::asText);
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
        JsonNode expectedTree = MAPPER_EXCLUDE_NULLS.readTree(expectedJson);
        JsonNode textTree = MAPPER_EXCLUDE_NULLS.readTree(actualJson);
        return expectedTree.equals(textTree);
    }

}
