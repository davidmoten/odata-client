package com.github.davidmoten.odata.client.generator.model;

public final class Annotation {

	private final org.oasisopen.odata.csdl.v4.Annotation annotation;

	public Annotation(org.oasisopen.odata.csdl.v4.Annotation annotation) {
		this.annotation = annotation;
	}

	public String getTerm() {
		return annotation.getTerm();
	}

	public String getString() {
		return annotation.getString();
	}

}
