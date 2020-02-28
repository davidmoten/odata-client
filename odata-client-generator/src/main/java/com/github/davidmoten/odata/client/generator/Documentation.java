package com.github.davidmoten.odata.client.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAnnotations;

public class Documentation {

    private Map<String, String> descriptions;
    private Map<String, String> longDescriptions;

    public Documentation(List<Schema> schemas) {
        this.descriptions = createDescriptions(schemas);
        this.longDescriptions = createLongDescriptions(schemas);
    }

    public Optional<String> getDescription(String typeWithNamespace) {
        return Optional.ofNullable(descriptions.get(typeWithNamespace));
    }

    public Optional<String> getPropertyDescription(String typeWithNamespace, String propertyName) {
        return Optional.ofNullable(descriptions.get(typeWithNamespace + "/" + propertyName));
    }
    
    public Optional<String> getLongDescription(String typeWithNamespace) {
        return Optional.ofNullable(longDescriptions.get(typeWithNamespace));
    }

    public Optional<String> getPropertyLongDescription(String typeWithNamespace, String propertyName) {
        return Optional.ofNullable(longDescriptions.get(typeWithNamespace + "/" + propertyName));
    }

    private static Map<String, String> createDescriptions(List<Schema> schemas) {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemas) {
            Util //
                    .types(schema, TAnnotations.class) //
                    .filter(a -> a.getTarget() != null) //
                    .forEach(a -> 
                        a //
                            .getAnnotation() //
                            .stream() //
                            .filter(x -> "Org.OData.Core.V1.Description".equals(x.getTerm())) //
                            .filter(x -> x.getString() != null) //
                            .forEach(x -> map.put(a.getTarget(), x.getString()))
                    );
        }
        return map;
    }
    
    private static Map<String, String> createLongDescriptions(List<Schema> schemas) {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemas) {
            Util //
                    .types(schema, TAnnotations.class) //
                    .filter(a -> a.getTarget() != null) //
                    .forEach(a -> 
                        a //
                            .getAnnotation() //
                            .stream() //
                            .filter(x -> "Org.OData.Core.V1.LongDescription".equals(x.getTerm())) //
                            .filter(x -> x.getString() != null) //
                            .forEach(x -> map.put(a.getTarget(), x.getString()))
                    );
        }
        return map;
    }

    @Override
    public String toString() {
        return "Documentation\n" + descriptions.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .map(x -> "  " + x.getKey() + " -> " + x.getValue()).collect(Collectors.joining("\n"));
    }

}
