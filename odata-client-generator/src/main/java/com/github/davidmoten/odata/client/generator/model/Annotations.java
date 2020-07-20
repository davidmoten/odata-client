package com.github.davidmoten.odata.client.generator.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.TAnnotations;

public final class Annotations {

    private final TAnnotations annotations;
    private final List<Annotation> list;

    public Annotations(TAnnotations annotations) {
        this.annotations = annotations;
        this.list = annotations //
                .getAnnotation() //
                .stream() //
                .map(Annotation::new) //
                .collect(Collectors.toList());
    }

    public TAnnotations value() {
        return annotations;
    }

    public Optional<String> getValue(String annotationType) {
        return annotations //
                .getAnnotation() //
                .stream() //
                .filter(x -> annotationType.equals(x.getTerm())) //
                .map(org.oasisopen.odata.csdl.v4.Annotation::getString) //
                .filter(Objects::nonNull) //
                .findFirst();
    }

    public List<Annotation> getValues() {
        return list;
    }

}
