package com.github.davidmoten.odata.client.generator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAnnotations;

import com.github.davidmoten.odata.client.generator.model.Annotation;
import com.github.davidmoten.odata.client.generator.model.Annotations;

public final class Documentation {

    private static final String DESCRIPTION = "Org.OData.Core.V1.Description";
    private static final String LONG_DESCRIPTION = "Org.OData.Core.V1.LongDescription";
    private final Map<String, Annotations> map;

    public Documentation(List<Schema> schemas) {
        this.map = createMap(schemas);
    }

    private static Map<String, Annotations> createMap(List<Schema> schemas) {
        return schemas //
                .stream() //
                .flatMap(schema -> Util.types(schema, TAnnotations.class)) //
                .map(Annotations::new) //
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

    public List<Annotation> getNonDescriptionAnnotations(String typeWithNamespace) {
        Annotations a = map.get(typeWithNamespace);
        if (a == null) {
            return Collections.emptyList();
        } else {
            return a //
                    .getValues() //
                    .stream() //
                    .filter(x -> !x.getTerm().equals(LONG_DESCRIPTION)
                            && !x.getTerm().equals(DESCRIPTION)) //
                    .collect(Collectors.toList());
        }
    }

    private Optional<String> description(String key) {
        Annotations a = map.get(key);
        if (a == null) {
            return Optional.empty();
        } else {
            Optional<String> v = a.getValue(LONG_DESCRIPTION);
            if (v.isPresent()) {
                return v;
            } else {
                return a.getValue(DESCRIPTION);
            }
        }
    }

}
