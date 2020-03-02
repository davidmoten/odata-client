package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.davidmoten.text.utils.WordWrap;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Generator;
import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Indent;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public abstract class Structure<T> {

	protected final T value;
	private final Class<T> cls;
	protected final Names names;

	public Structure(T value, Class<T> cls, Names names) {
		this.value = value;
		this.cls = cls;
		this.names = names;
	}

	public abstract Structure<T> create(T t);

	public abstract String getName();

	public abstract String getBaseType();

	public final boolean hasBaseType() {
		return getBaseType() != null;
	}

	public abstract boolean isAbstract();

	public abstract List<TProperty> getProperties();

	public final List<Property> getProperties2() {
		return getProperties().stream().map(x -> new Property(x, names)).collect(Collectors.toList());
	}

	public abstract List<TNavigationProperty> getNavigationProperties();

	public abstract boolean isEntityType();

	public final List<? extends Structure<T>> getHeirarchy() {
		List<Structure<T>> a = new LinkedList<>();
		a.add(create(value));
		Structure<T> st = this;
		while (true) {
			if (st.getBaseType() == null) {
				return a;
			} else {
				String baseTypeSimpleName = names.getSimpleTypeNameFromTypeWithNamespace(st.getBaseType());
				// TODO make a map for lookup to increase perf
				st = names.getSchemas() //
						.stream() //
						.flatMap(schema -> Util.types(schema, cls)) //
						.map(this::create) //
						.filter(x -> x.getName().equals(baseTypeSimpleName)) //
						.findFirst() //
						.get();
				a.add(0, st);
			}
		}
	}

	public final List<Field> getFields(Imports imports) {
		List<Field> list = getHeirarchy() //
				.stream() //
				.flatMap(z -> z.getProperties() //
						.stream() //
						.flatMap(x -> toFields(x, imports)))
				.collect(Collectors.toList());
		return list;
	}

	public static final class FieldName {
		public final String name;
		public final String fieldName;

		FieldName(String name, String fieldName) {
			this.name = name;
			this.fieldName = fieldName;
		}

	}

	public final List<FieldName> getFieldNames() {
		return getHeirarchy() //
				.stream() //
				.flatMap(z -> z.getProperties() //
						.stream() //
						.map(x -> new FieldName(x.getName(), toFieldName(x))))
				.collect(Collectors.toList());
	}

	private String toFieldName(TProperty x) {
		return Names.getIdentifier(x.getName());
	}

	private final Stream<Field> toFields(TProperty x, Imports imports) {
		Field a = new Field(x.getName(), Names.getIdentifier(x.getName()), x.getName(),
				names.toImportedFullClassName(x, imports));
		if (names.isCollection(x) && !names.isEntityWithNamespace(names.getType(x))) {
			Field b = new Field(x.getName(), Names.getIdentifier(x.getName()) + "NextLink", x.getName() + "@nextLink",
					imports.add(String.class));
			return Stream.of(a, b);
		} else {
			return Stream.of(a);
		}
	}

	public String getExtendsClause(Imports imports) {
		if (getBaseType() != null) {
			return " extends " + imports.add(names.getFullClassNameFromTypeWithNamespace(getBaseType()));
		} else {
			return "";
		}
	}

	private static String encodeJavadoc(String x) {
		return x.replace("@", "&#064;") //
				.replace("\\", "{@literal \\}") //
				.replace("<", "&lt;") //
				.replace(">", "@gt;");
	}

	public void printPropertyJavadoc(PrintWriter p, Indent indent, String name) {
		printJavadoc(p, indent, getFullType() + "/" + name, Optional.empty());
	}

	private final void printJavadoc(PrintWriter p, Indent indent, String key, Optional<String> preamble) {
		Optional<String> text = names.getDocumentation().getDescription(key);
		List<Annotation> list = names.getDocumentation().getNonDescriptionAnnotations(key);
		boolean hasText = text.isPresent() || !list.isEmpty();
		if (hasText) {
			p.format("\n%s/**\n", indent);
			if (preamble.isPresent()) {
				p.format("%s * <p>\n", indent);
				p.format("%s * %s\n", indent, preamble.get());
			}
		}
		if (text.isPresent()) {
			String s = encodeJavadoc(wrap(text.get()) //
					.replace("\n", String.format("\n%s * ", indent)));
			p.format("%s * %s\n", indent, s);
		}
		list.forEach(a -> {
			p.format("%s * <p>\n", indent);
			p.format("%s * <b>%s</b>\n", indent, a.getTerm());
			if (a.getString().isPresent()) {
				p.format("%s * <p>\n", indent);
				p.format("%s * %s\n", indent, a.getString().get());
			}
			if (a.getBool().isPresent()) {
				p.format("%s * <p>\n", indent);
				p.format("%s * %s\n", indent, a.getBool().get());
			}
			for (String record : a.getRecords()) {
				p.format("%s * <p>\n", indent);
				p.format("%s * %s\n", indent, record);
			}
		});
		if (hasText) {
			p.format("%s */", indent);
		}
	}

	public final void printJavadoc(PrintWriter p, Indent indent) {
		printJavadoc(p, indent, getFullType(), Optional.empty());
	}

	public void printMutatePropertyJavadoc(PrintWriter p, Indent indent, String name) {
		String s = "Returns an immutable copy with just the {@code " + name
				+ "} field changed. Field description below.";
		printJavadoc(p, indent, name, Optional.of(s));
	}

	private static String wrap(String s) {
		return WordWrap //
				.from(s) //
				.breakWords(false) //
				.maxWidth(Generator.MAX_JAVADOC_WIDTH) //
				.newLine("\n") //
				.wrap() //
				.trim();
	}

	public abstract File getClassFile();

	public abstract String getSimpleClassName();

	public abstract String getPackage();

	public abstract String getFullType();

	public abstract File getClassFileCollectionRequest();

}
