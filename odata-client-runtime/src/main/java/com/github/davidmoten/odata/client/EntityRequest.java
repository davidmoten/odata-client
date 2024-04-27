package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public abstract class EntityRequest<T extends ODataEntityType> {

    private final Class<T> cls;
    protected final ContextPath contextPath;
    private final Optional<Object> value;
    private final boolean isMediaEntityOrHasStreamProperty;

    public EntityRequest(Class<T> cls, ContextPath contextPath, Optional<Object> value, boolean isMediaEntityOrHasStreamProperty) {
        this.cls = cls;
        this.contextPath = contextPath;
        this.value = value;
        this.isMediaEntityOrHasStreamProperty = isMediaEntityOrHasStreamProperty;
    }

    T get(EntityRequestOptions<T> options) {
        if (value.isPresent()) {
            String json = Serializer.INSTANCE.serialize(value.get());
            Class<? extends T> subClass = RequestHelper.getSubClass(contextPath, contextPath.context().schemas(),
                    cls, json);
            return Serializer.INSTANCE.deserialize(json, subClass, contextPath, false);
        } else {
            return RequestHelper.get(contextPath, cls, options);
        }
    }

    void delete(EntityRequestOptions<T> options) {
        RequestHelper.delete(contextPath, options);
    }

    T patch(EntityRequestOptions<T> options, T entity) {
        return RequestHelper.patch(entity, contextPath, options);
    }

    T put(EntityRequestOptions<T> options, T entity) {
        return RequestHelper.put(entity, contextPath, options);
    }

    public T get() {
        return builder().get();
    }

    public void delete() {
        builder().delete();
    }

    public T patch(T entity) {
        return builder().patch(entity);
    }

    public T put(T entity) {
        return builder().put(entity);
    }

    public EntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return builder().requestHeader(key, value);
    }

    public EntityRequestOptionsBuilder<T> query(String name, String value) {
        return builder().query(name, value);
    }
    
    public EntityRequestOptionsBuilder<T> select(String clause) {
        return builder().select(clause);
    }

    public EntityRequestOptionsBuilder<T> expand(String clause) {
        return builder().expand(clause);
    }

    public EntityRequestOptionsBuilder<T> metadataFull() {
        return builder().metadataFull();
    }

    public EntityRequestOptionsBuilder<T> metadataMinimal() {
        return builder().metadataMinimal();
    }

    public EntityRequestOptionsBuilder<T> metadataNone() {
        return builder().metadataNone();
    }
    
    private EntityRequestOptionsBuilder<T> builder() {
        return new EntityRequestOptionsBuilder<T>(this, isMediaEntityOrHasStreamProperty);
    }

}
