package com.github.davidmoten.odata.client.generator.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBElement;

import org.oasisopen.odata.csdl.v4.TPropertyValue;
import org.oasisopen.odata.csdl.v4.TRecordExpression;

import com.github.davidmoten.odata.client.generator.Util;

public final class Annotation {

    private final org.oasisopen.odata.csdl.v4.Annotation annotation;

    public Annotation(org.oasisopen.odata.csdl.v4.Annotation annotation) {
        this.annotation = annotation;
    }

    public String getTerm() {
        return annotation.getTerm();
    }

    public Optional<String> getString() {
        return Optional.ofNullable(annotation.getString());
    }

    public List<String> getRecords() {
        List<String> bools = Util
                .filter(annotation.getAnnotationOrBinaryOrBool(), JAXBElement.class) //
                .map(JAXBElement::getValue) //
                .filter(x -> x instanceof TRecordExpression) //
                .map(x -> (TRecordExpression) x) //
                .flatMap(x -> Util.filter(x.getPropertyValueOrAnnotation(), TPropertyValue.class)) //
                .filter(x -> x.isBool() != null) //
                .map(x -> x.getProperty() + " = " + x.isBool()) //
                .collect(Collectors.toList());
        List<String> enums = Util
                .filter(annotation.getAnnotationOrBinaryOrBool(), JAXBElement.class) //
                .map(JAXBElement::getValue) //
                .filter(x -> x instanceof TRecordExpression) //
                .map(x -> (TRecordExpression) x) //
                .flatMap(x -> Util.filter(x.getPropertyValueOrAnnotation(), TPropertyValue.class)) //
                .filter(x -> x.getEnumMember() != null && !x.getEnumMember().isEmpty()) //
                .map(x -> x.getProperty() + " = " + toString(x.getEnumMember())) //
                .collect(Collectors.toList());
        bools.addAll(enums);
        return bools;
    }

    private static String toString(List<String> list) {
        if (list.isEmpty()) {
            return "";
        } else if (list.size() == 1) {
            return String.valueOf(list.get(0));
        } else {
            return String.valueOf(list);
        }
    }

    public Optional<Boolean> getBool() {
        return Optional.ofNullable(annotation.isBool());
    }

}
