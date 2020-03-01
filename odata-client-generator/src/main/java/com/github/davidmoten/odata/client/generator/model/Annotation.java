package com.github.davidmoten.odata.client.generator.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

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
		return Util.filter(annotation.getAnnotationOrBinaryOrBool(), JAXBElement.class) //
				.map(x -> x.getValue()) //
				.filter(x -> x instanceof TRecordExpression) //
				.map(x -> (TRecordExpression) x) //
				.flatMap(x -> Util.filter(x.getPropertyValueOrAnnotation(), TPropertyValue.class)) //
				.filter(x -> x.isBool() != null) //
				.map(x -> x.getProperty() + " = " + x.isBool()) //
				.collect(Collectors.toList());
	}

	public Optional<Boolean> getBool() {
		return Optional.ofNullable(annotation.isBool());
	}

}
