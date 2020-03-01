package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.text.utils.WordWrap;
import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TEntityKeyElement;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Generator;
import com.github.davidmoten.odata.client.generator.Indent;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public final class EntityType extends Structure<TEntityType> {

	private Schema schema;

	public EntityType(TEntityType c, Names names) {
		super(c, TEntityType.class, names);
	}

	@Override
	public String getName() {
		return value.getName();
	}

	@Override
	public String getBaseType() {
		return value.getBaseType();
	}

	@Override
	public List<TProperty> getProperties() {
		return Util.filter(value.getKeyOrPropertyOrNavigationProperty(), TProperty.class).collect(Collectors.toList());
	}

	@Override
	public List<TNavigationProperty> getNavigationProperties() {
		return Util.filter(value.getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class)
				.collect(Collectors.toList());
	}

	@Override
	public Structure<TEntityType> create(TEntityType t) {
		return new EntityType(t, names);
	}

	@Override
	public boolean isEntityType() {
		return true;
	}

	@Override
	public String getSimpleClassName() {
		return names.getSimpleClassNameEntity(schema(), value.getName());
	}

	@Override
	public String getFullType() {
		return names.getFullTypeFromSimpleType(schema(), getName());
	}

	@Override
	public File getClassFile() {
		return names.getClassFileEntity(schema(), getName());
	}

	public File getClassFileEntityRequest() {
		return names.getClassFileEntityRequest(schema(), getName());
	}

	@Override
	public File getClassFileCollectionRequest() {
		return names.getClassFileEntityCollectionRequest(schema(), getName());
	}

	@Override
	public String getPackage() {
		return names.getPackageEntity(schema());
	}

	private Schema schema() {
		if (schema == null) {
			schema = names.getSchema(value);
		}
		return schema;
	}

	public String getFullClassNameEntity() {
		return names.getFullClassNameFromTypeWithoutNamespace(schema(), getName());
	}

	public String getSimpleClassNameEntityRequest() {
		return names.getSimpleClassNameEntityRequest(schema(), getName());
	}

	public String getPackageEntityRequest() {
		return names.getPackageEntityRequest(schema());
	}

	public File getDirectoryEntity() {
		return names.getDirectoryEntity(schema());
	}

	private List<KeyElement> getKeysLocal() {
		return Util.filter(value.getKeyOrPropertyOrNavigationProperty(), TEntityKeyElement.class) //
				.map(x -> new KeyElement(x, this, names)) //
				.collect(Collectors.toList());
	}

	public List<KeyElement> getKeys() {
		return getHeirarchy() //
				.stream() //
				.flatMap(x -> ((EntityType) x).getKeysLocal().stream()) //
				.collect(Collectors.toList());
	}

	public KeyElement getFirstKey() {
		if (getKeys().isEmpty()) {
			throw new IllegalStateException("Entity " + getName() + " has no keys!");
		}
		return getKeys().get(0);
	}

	public String getFullClassNameSchema() {
		return names.getFullClassNameSchemaInfo(schema());
	}

	@Override
	public boolean isAbstract() {
		return value.isAbstract();
	}

	public boolean hasStream() {
		return value.isHasStream();
	}

	public String getFullClassNameEntityRequest() {
		return getPackageEntityRequest() + "." + getSimpleClassNameEntityRequest();
	}

	public void printJavadoc(PrintWriter p, Indent indent) {
		Optional<String> text = names.getDocumentation().getDescription(getFullType());
		if (text.isPresent()) {
			p.format("\n%s /**\n", indent);
			String s = wrap(text.get()) //
					.replace("\n", "\n" + indent + " * ") //
					.replace("@", "{@literal @}") //
					.replace("<", "&lt;") //
					.replace(">", "&gt;");
			p.format("%s * %s\n", indent, s);
		}
		List<Annotation> list = names.getDocumentation().getNonDescriptionAnnotations(getFullType());
		if (!text.isPresent() && !list.isEmpty()) {
			p.format("%s /**\n", indent);
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
			for (String record: a.getRecords()) {
				p.format("%s * <p>\n", indent);
				p.format("%s * %s\n", indent, record);
			}
		});
		if (text.isPresent() || !list.isEmpty()) {
			p.format("%s */\n", indent);
		}
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

}
