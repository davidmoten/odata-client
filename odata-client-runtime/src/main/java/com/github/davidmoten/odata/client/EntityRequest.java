package com.github.davidmoten.odata.client;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public abstract class EntityRequest<T extends ODataEntityType> implements HasSelect<EntityRequestOptionsBuilder<T>> {

	private final Class<T> cls;
	protected final ContextPath contextPath;
	private final SchemaInfo schemaInfo;

	public EntityRequest(Class<T> cls, ContextPath contextPath, SchemaInfo schemaInfo) {
		this.cls = cls;
		this.contextPath = contextPath;
		this.schemaInfo = schemaInfo;
	}

	T get(EntityRequestOptions<T> options) {
		return RequestHelper.get(contextPath, cls, options, schemaInfo);
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
		return new EntityRequestOptionsBuilder<T>(this).get();
	}

	public void delete() {
		new EntityRequestOptionsBuilder<T>(this).delete();
	}

	public T patch(T entity) {
		return new EntityRequestOptionsBuilder<T>(this).patch(entity);
	}

	public T put(T entity) {
		return new EntityRequestOptionsBuilder<T>(this).put(entity);
	}

	public EntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
		return new EntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
	}

	@Override
	public EntityRequestOptionsBuilder<T> select(String clause) {
		return new EntityRequestOptionsBuilder<T>(this).select(clause);
	}

	public EntityRequestOptionsBuilder<T> expand(String clause) {
		return new EntityRequestOptionsBuilder<T>(this).expand(clause);
	}

	public EntityRequestOptionsBuilder<T> metadataFull() {
		return new EntityRequestOptionsBuilder<T>(this).metadataFull();
	}

	public EntityRequestOptionsBuilder<T> metadataMinimal() {
		return new EntityRequestOptionsBuilder<T>(this).metadataMinimal();
	}

	public EntityRequestOptionsBuilder<T> metadataNone() {
		return new EntityRequestOptionsBuilder<T>(this).metadataNone();
	}

}
