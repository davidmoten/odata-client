package com.github.davidmoten.odata.client.generator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAnnotations;

import com.github.davidmoten.odata.client.generator.model.Annotations;

public final class Documentation {

	private final Map<String, Annotations> map;

	public Documentation(List<Schema> schemas) {
		this.map = createMap(schemas);
	}

	private static Map<String, Annotations> createMap(List<Schema> schemas) {
		return schemas //
				.stream() //
				.flatMap(schema -> Util.types(schema, TAnnotations.class)) //
				.map(a -> new Annotations(a)) //
				.collect(Collectors.toMap(a -> a.value().getTarget(), a -> a));

	}

	public Optional<String> getDescription(String typeWithNamespace) {
		return getDescriptionWithKey(typeWithNamespace);
	}

	public Optional<String> getPropertyDescription(String typeWithNamespace, String propertyName) {
		return description(typeWithNamespace + "/" + propertyName);
	}

	private Optional<String> getDescriptionWithKey(String typeWithNamespace) {
		return description(typeWithNamespace);
	}

	private Optional<String> description(String key) {
		Annotations a = map.get(key);
		if (a == null) {
			return Optional.empty();
		} else {
			Optional<String> v = a.getValue("Org.OData.Core.V1.LongDescription");
			if (v.isPresent()) {
				return v;
			} else {
				return a.getValue("Org.OData.Core.V1.Description");
			}
		}
	}

}
