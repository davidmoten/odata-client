package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityContainer;
import org.oasisopen.odata.csdl.v4.TEntitySet;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;
import org.oasisopen.odata.csdl.v4.TFunction;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TNavigationPropertyBinding;
import org.oasisopen.odata.csdl.v4.TProperty;
import org.oasisopen.odata.csdl.v4.TSingleton;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.ActionRequestNoReturn;
import com.github.davidmoten.odata.client.ActionRequestReturningNonCollection;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.CollectionPageNonEntityRequest;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.EntityRequest;
import com.github.davidmoten.odata.client.FunctionRequestReturningNonCollection;
import com.github.davidmoten.odata.client.HasContext;
import com.github.davidmoten.odata.client.HttpRequestOptions;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.NameValue;
import com.github.davidmoten.odata.client.ODataEntityType;
import com.github.davidmoten.odata.client.ODataType;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.StreamProvider;
import com.github.davidmoten.odata.client.StreamUploader;
import com.github.davidmoten.odata.client.StreamUploaderChunked;
import com.github.davidmoten.odata.client.TestingService.BuilderBase;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;
import com.github.davidmoten.odata.client.UploadStrategy;
import com.github.davidmoten.odata.client.annotation.NavigationProperty;
import com.github.davidmoten.odata.client.annotation.Property;
import com.github.davidmoten.odata.client.generator.Names.SchemaAndType;
import com.github.davidmoten.odata.client.generator.model.Action;
import com.github.davidmoten.odata.client.generator.model.Action.Parameter;
import com.github.davidmoten.odata.client.generator.model.Action.ReturnType;
import com.github.davidmoten.odata.client.generator.model.ComplexType;
import com.github.davidmoten.odata.client.generator.model.EntitySet;
import com.github.davidmoten.odata.client.generator.model.EntityType;
import com.github.davidmoten.odata.client.generator.model.Field;
import com.github.davidmoten.odata.client.generator.model.Function;
import com.github.davidmoten.odata.client.generator.model.HasNameJavaHasNullable;
import com.github.davidmoten.odata.client.generator.model.KeyElement;
import com.github.davidmoten.odata.client.generator.model.Method;
import com.github.davidmoten.odata.client.generator.model.Structure;
import com.github.davidmoten.odata.client.generator.model.Structure.FieldName;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.Checks;
import com.github.davidmoten.odata.client.internal.EdmSchemaInfo;
import com.github.davidmoten.odata.client.internal.ParameterMap;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;
import com.github.davidmoten.odata.client.internal.UnmappedFields;

public final class Generator {

	private static final String COLLECTION_PREFIX = "Collection(";
	// private final Schema schema;
	private final Names names;
	private final List<Schema> schemas;

	public Generator(Options options, List<Schema> schemas) {
		this.schemas = schemas;
		this.names = Names.create(schemas, options);
	}

	public void generate() {

		log("-----------------------------------");
		log("Generating code for namespaces:");
		schemas //
				.stream() //
				.forEach(s -> log("  " + s.getNamespace()));

		schemas //
				.stream() //
				.flatMap(s -> Util.filter(s.getComplexTypeOrEntityTypeOrTypeDefinition(), TEntityType.class)
						.map(t -> new SchemaAndType<TEntityType>(s, t))) //
				.map(x -> names.toTypeWithNamespace(x.schema, x.type.getName())) //
				.forEach(System.out::println);

		log("-----------------------------------");
		log("replacing aliases");
		Util.replaceAliases(schemas);

		log("finding collection types");
		Set<String> collectionTypes = findTypesUsedInCollections(names, schemas);

		for (Schema schema : schemas) {

			log("generating for namespace=" + schema.getNamespace());

			log("  creating maps");
			Map<String, List<Action>> typeActions = createTypeActions(schema, names, false);
			log("    entity actions count = " + typeActions.size());

			Map<String, List<Function>> typeFunctions = createTypeFunctions(schema, names, false);
			log("    entity functions count = " + typeFunctions.size());

			Map<String, List<Action>> collectionTypeActions = createTypeActions(schema, names, true);
			log("    collection actions count = " + collectionTypeActions.size());

			Map<String, List<Function>> collectionTypeFunctions = createTypeFunctions(schema, names, true);
			System.out.println("    collection functions count = " + collectionTypeFunctions.size());

			log("  writing schema info");
			writeSchemaInfo(schema);

			// write enums
			log("  writing enums");
			Util.types(schema, TEnumType.class) //
					.forEach(x -> writeEnum(schema, x));

			// write entityTypes
			log("  writing entities");
			Util.types(schema, TEntityType.class) //
					.forEach(x -> writeEntity(x, typeActions, typeFunctions));

			// write complexTypes
			log("  writing complex types");
			Util.types(schema, TComplexType.class) //
					.forEach(x -> writeComplexType(schema, x));

			// write entity collection requests
			log("  writing entity collection requests");
			Util.types(schema, TEntityType.class) //
					.forEach(x -> writeEntityCollectionRequest(schema, x, collectionTypeActions,
							collectionTypeFunctions, collectionTypes));

			log("writing entity set requests");
			Util.types(schema, TEntityContainer.class) //
					.flatMap(c -> Util //
							.filter(c.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
							.map(x -> new Pair<TEntityContainer, TEntitySet>(c, x))) //
					.forEach(x -> writeEntitySet(schema, x));

			// write containers
			log("  writing container");
			Util.types(schema, TEntityContainer.class) //
					.forEach(x -> writeContainer(schema, x));

			// write single requests
			log("  writing entity requests");
			Util.types(schema, TEntityType.class) //
					.forEach(x -> writeEntityRequest(schema, x, typeActions, typeFunctions));

			log("  writing complex type requests");
			Util.types(schema, TComplexType.class) //
					.forEach(x -> writeComplexTypeRequest(schema, x));
		}
	}

	private static final class Pair<A, B> {
		final A a;
		final B b;

		Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}

	private void writeEntitySet(Schema schema, Pair<TEntityContainer, TEntitySet> pair) {
		EntitySet t = new EntitySet(schema, pair.a, pair.b, names);
		t.getDirectoryEntitySet().mkdirs();
		Imports imports = new Imports(t.getFullClassNameEntitySet());
		Indent indent = new Indent();

		StringWriter w = new StringWriter();
		try (PrintWriter p = new PrintWriter(w)) {
			p.format("package %s;\n\n", t.getPackage());
			p.format("IMPORTSHERE");

			String baseCollectionClassName = t.getBaseCollectionRequestClassName(imports);
			p.format("%spublic final class %s extends %s {\n", //
					indent, //
					t.getSimpleClassNameEntitySet(), //
					baseCollectionClassName);
			indent.right();
			p.format("\n%spublic %s(%s contextPath) {\n", //
					indent, //
					t.getSimpleClassNameEntitySet(), //
					imports.add(ContextPath.class));
			p.format("%ssuper(contextPath);\n", indent.right());
			p.format("%s}\n", indent.left());

			// write navigation property bindings
			Util.filter( //
					pair.b.getNavigationPropertyBindingOrAnnotation(), //
					TNavigationPropertyBinding.class) //
					.forEach(b -> {
						String methodName = t.getMethodName(b);
						EntitySet referredEntitySet = t.getReferredEntitySet(b.getTarget());
						String returnClassName = referredEntitySet.getFullClassNameEntitySet();
						p.format("\n%spublic %s %s() {\n", indent, imports.add(returnClassName), methodName);
						p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", //
								indent.right(), //
								imports.add(referredEntitySet.getFullClassNameEntitySet()), //
								b.getPath());
						p.format("%s}\n", indent.left());
					});
			p.format("%s}\n", indent.left());
			writeToFile(imports, w, t.getClassFile());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Set<String> findTypesUsedInCollections(Names names, List<Schema> schemas) {
		return schemas //
				.stream() //
				.flatMap(schema -> {

					List<String> types = new ArrayList<>();
					Util.types(schema, TEntityType.class) //
							.flatMap(t -> Stream.concat(Util //
									.filter(t.getKeyOrPropertyOrNavigationProperty(), TProperty.class) //
									.filter(x -> names.isCollection(x)) //
									.map(x -> names.getType(x)),
									Util //
											.filter(t.getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class)
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)))) //
							.forEach(x -> types.add(x));
					Util.types(schema, TComplexType.class) //
							.flatMap(t -> Stream.concat(//
									Util //
											.filter(t.getPropertyOrNavigationPropertyOrAnnotation(), TProperty.class)
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)),
									Util //
											.filter(t.getPropertyOrNavigationPropertyOrAnnotation(),
													TNavigationProperty.class)
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)))) //
							.forEach(x -> types.add(x));
					Util.types(schema, TAction.class) //
							.flatMap(t -> Stream.concat( //
									Util //
											.filter(t.getParameterOrAnnotationOrReturnType(),
													TActionFunctionParameter.class) //
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)),
									Util //
											.filter(t.getParameterOrAnnotationOrReturnType(),
													TActionFunctionReturnType.class)
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)))) //
							.forEach(x -> types.add(x));
					Util.types(schema, TFunction.class) //
							.flatMap(t -> Stream.concat( //
									Util //
											.filter(t.getParameterOrAnnotation(), TActionFunctionParameter.class) //
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)),
									Util //
											.filter(t.getParameterOrAnnotation(), TActionFunctionReturnType.class)
											.filter(x -> names.isCollection(x)) //
											.map(x -> names.getType(x)))) //
							.forEach(x -> types.add(x));
					Util.types(schema, TEntityContainer.class) //
							.flatMap(t -> Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class)) //
							.flatMap(t -> Stream.concat(Stream.of(t.getEntityType()), Util //
									.filter(t.getNavigationPropertyBindingOrAnnotation(),
											TNavigationPropertyBinding.class) //
									.map(x -> x.getPath())))
							.forEach(x -> types.add(x));
					return types.stream();
				}) //
				.map(x -> names.getInnerType(x)) //
				.collect(Collectors.toSet());
	}

	private void log(Object s) {
		System.out.println(String.valueOf(s));
	}

	private Map<String, List<Action>> createTypeActions(Schema schema, Names names, boolean collectionsOnly) {
		return createMap(TAction.class, schema, names, action -> new Action(action, names), collectionsOnly);
	}

	private Map<String, List<Function>> createTypeFunctions(Schema schema, Names names, boolean collectionsOnly) {
		return createMap(TFunction.class, schema, names, function -> new Function(function, names), collectionsOnly);
	}

	@SuppressWarnings("unchecked")
	private <T, S extends Method> Map<String, List<S>> createMap(Class<T> cls, Schema schema, Names names,
			java.util.function.Function<T, S> mapper, boolean collectionsOnly) {
		Map<String, List<S>> map = new HashMap<>();
		Util.types(schema, cls) //
				.forEach(method -> {
					S a = mapper.apply(method);
					if ((!collectionsOnly && !a.isBoundToCollection())
							|| (collectionsOnly && a.isBoundToCollection())) {
						a.getBoundTypeWithNamespace() //
								.ifPresent(x -> {
									List<S> list = map.get(x);
									if (list == null) {
										map.put(x, Lists.newArrayList(a));
									} else {
										list.add(a);
									}
								});
					}
				});
		return map;
	}

	private void writeComplexTypeRequest(Schema schema, TComplexType x) {
		// TODO are these used?
	}

	private void writeSchemaInfo(Schema schema) {
		names.getDirectorySchema(schema).mkdirs();
		String simpleClassName = names.getSimpleClassNameSchema(schema);
		Imports imports = new Imports(names.getFullClassNameSchema(schema));
		Indent indent = new Indent();
		try {
			StringWriter w = new StringWriter();
			try (PrintWriter p = new PrintWriter(w)) {
				p.format("package %s;\n\n", names.getPackageSchema(schema));
				p.format("IMPORTSHERE");
				p.format("public enum %s implements %s {\n\n", simpleClassName, imports.add(SchemaInfo.class));

				// add enum
				p.format("%sINSTANCE;\n\n", indent.right());

				// add fields for entities map
				p.format("%sprivate final %s<%s, %s<? extends %s>> classes = new %s<>();\n\n", //
						indent, //
						imports.add(Map.class), //
						imports.add(String.class), //
						imports.add(Class.class), //
						imports.add(ODataType.class), //
						imports.add(HashMap.class));

				// add private constructor
				p.format("%sprivate %s() {\n", indent, simpleClassName);
				indent.right();

				// write all schemas classes into map not just the current one
				// TODO use one SchemasInfo.class
				names //
						.getSchemas() //
						.stream() //
						.flatMap(
								sch -> Util.filter(sch.getComplexTypeOrEntityTypeOrTypeDefinition(), TEntityType.class)) //
						.forEach(x -> {
							Schema sch = names.getSchema(x);
							p.format("%sclasses.put(\"%s\", %s.class);\n", indent,
									names.getFullTypeFromSimpleType(sch, x.getName()),
									imports.add(names.getFullClassNameEntity(sch, x.getName())));
						});
				names //
						.getSchemas() //
						.stream() //
						.flatMap(sch -> Util.filter(sch.getComplexTypeOrEntityTypeOrTypeDefinition(),
								TComplexType.class)) //
						.forEach(x -> {
							Schema sch = names.getSchema(x);
							p.format("%sclasses.put(\"%s\", %s.class);\n", indent,
									names.getFullTypeFromSimpleType(sch, x.getName()),
									imports.add(names.getFullClassNameComplexType(sch, x.getName())));
						});
				indent.left();

				p.format("%s}\n\n", indent);

				// add method
				p.format("%s@%s\n", indent, imports.add(Override.class));
				p.format("%spublic %s<? extends %s> getClassFromTypeWithNamespace(%s name) {\n", //
						indent, //
						imports.add(Class.class), //
						imports.add(ODataType.class), //
						imports.add(String.class));
				p.format("%sreturn classes.get(name);\n", indent.right());
				p.format("%s}\n\n", indent.left());

				// close class
				p.format("}\n");
			}
			byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
			Files.write(names.getClassFileSchema(schema).toPath(), bytes);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

	}

	private void writeEnum(Schema schema, TEnumType t) {
		names.getDirectoryEnum(schema).mkdirs();
		String simpleClassName = names.getSimpleClassNameEnum(schema, t.getName());
		Imports imports = new Imports(names.getFullClassNameEnum(schema, t.getName()));
		Indent indent = new Indent();
		try {
			StringWriter w = new StringWriter();
			try (PrintWriter p = new PrintWriter(w)) {
				p.format("package %s;\n\n", names.getPackageEnum(schema));
				p.format("IMPORTSHERE");
				p.format("public enum %s implements %s {\n", simpleClassName,
						imports.add(com.github.davidmoten.odata.client.Enum.class));

				// add members
				indent.right();
				String s = Util.filter(t.getMemberOrAnnotation(), TEnumTypeMember.class) //
						.map(x -> String.format("%s@%s(\"%s\")\n%s%s(\"%s\", \"%s\")", //
								indent, //
								imports.add(JsonProperty.class), //
								x.getName(), //
								indent, //
								names.getEnumInstanceName(t, x.getName()), //
								x.getName(), //
								x.getValue()))
						.collect(Collectors.joining(",\n\n"));
				indent.left();
				p.format("\n%s;\n\n", s);

				// add fields
				p.format("%sprivate final %s name;\n", indent.right(), imports.add(String.class));
				p.format("%sprivate final %s value;\n\n", indent, imports.add(String.class));

				// add constructor
				p.format("%sprivate %s(%s name, %s value) {\n", indent, simpleClassName, imports.add(String.class),
						imports.add(String.class));
				p.format("%sthis.name = name;\n", indent.right());
				p.format("%sthis.value = value;\n", indent);
				p.format("%s}\n\n", indent.left());

				// add methods
				p.format("%s@%s\n", indent, imports.add(Override.class));
				p.format("%spublic %s enumName() {\n", indent, imports.add(String.class));
				p.format("%sreturn name;\n", indent.right());
				p.format("%s}\n\n", indent.left());

				p.format("%s@%s\n", indent, imports.add(Override.class));
				p.format("%spublic %s enumValue() {\n", indent, imports.add(String.class));
				p.format("%sreturn value;\n", indent.right());
				p.format("%s}\n\n", indent.left());

				// close class
				p.format("}\n");
			}
			byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
			Files.write(names.getClassFileEnum(schema, t.getName()).toPath(), bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeEntity(TEntityType entityType, Map<String, List<Action>> typeActions,
			Map<String, List<Function>> typeFunctions) {
		EntityType t = new EntityType(entityType, names);
		t.getDirectoryEntity().mkdirs();
		String simpleClassName = t.getSimpleClassName();
		Imports imports = new Imports(t.getFullClassNameEntity());
		Indent indent = new Indent();

		StringWriter w = new StringWriter();
		try (PrintWriter p = new PrintWriter(w)) {
			p.format("package %s;\n\n", t.getPackage());
			p.format("IMPORTSHERE");

			t.printJavadoc(p, indent);
			printPropertyOrder(imports, p, t.getProperties());
			printJsonIncludesNonNull(indent, imports, p);
			p.format("public class %s%s implements %s {\n", simpleClassName, t.getExtendsClause(imports),
					imports.add(ODataEntityType.class));

			indent.right();
			if (!t.hasBaseType()) {
				addContextPathInjectableField(imports, indent, p);
				addUnmappedFieldsField(imports, indent, p);
				addChangedFieldsField(imports, indent, p);
			}

			p.format("\n%s@%s\n", indent, imports.add(Override.class));
			p.format("%spublic String odataTypeName() {\n", indent);
			p.format("%sreturn \"%s\";\n", indent.right(), t.getFullType());
			p.format("%s}\n", indent.left());

			// add other fields
			printPropertyFields(imports, indent, p, t.getProperties(), t.hasBaseType());
			
			// write constructor
			writeNoArgsConstructor(simpleClassName, indent, p, t.hasBaseType());

			writeBuilder(t, simpleClassName, imports, indent, p);
			
			printSelectBuilder(imports, indent, p, t.getProperties());

			p.format("\n%s@%s\n", indent, imports.add(Override.class));
			p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
			p.format("%spublic %s getChangedFields() {\n", indent, imports.add(ChangedFields.class));
			p.format("%sreturn changedFields;\n", indent.right());
			p.format("%s}\n", indent.left());
			String nullCheck = fieldNames(t) //
					.stream() //
					.map(f -> f + " != null") //
					.collect(Collectors.joining(" && "));
			if (!nullCheck.isEmpty()) {
				nullCheck = " && " + nullCheck;
			}
			p.format("\n%s@%s\n", indent, imports.add(Override.class));
			p.format("%spublic void postInject(boolean addKeysToContextPath) {\n", indent);
			p.format("%sif (addKeysToContextPath%s) {\n", indent.right(), nullCheck);
			p.format("%scontextPath = contextPath.clearQueries()%s;\n", indent.right(), getAddKeys(t, imports));
			p.format("%s}\n", indent.left());
			p.format("%s}\n", indent.left());

			Set<String> methodNames = new HashSet<>();
			// write property getter and setters
			printPropertyGetterAndSetters(t, imports, indent, p, simpleClassName, t.getFullType(), t.getProperties(),
					true, methodNames);
			addInheritedPropertyNames(t, methodNames);
			printNavigationPropertyGetters(t, imports, indent, p, t.getNavigationProperties(), methodNames);

			addUnmappedFieldsSetterAndGetter(imports, indent, p, methodNames);

			if (t.hasStream()) {
				p.format("\n%s/**\n", indent);
				p.format("%s * If suitable metadata found a StreamProvider is returned otherwise returns\n", indent);
				p.format("%s * {@code Optional.empty()}. Normally for a stream to be available this entity\n", indent);
				p.format("%s * needs to have been hydrated with full metadata. Consider calling the builder\n", indent);
				p.format("%s * method {@code .metadataFull()} when getting this instance (either directly or\n",
						indent);
				p.format("%s * as part of a collection).\n", indent);
				p.format("%s *\n", indent);
				p.format("%s * @return StreamProvider if suitable metadata found otherwise returns\n", indent);
				p.format("%s *         {@code Optional.empty()}\n", indent);
				p.format("%s */\n", indent);
				p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
				p.format("%spublic %s<%s> getStream() {\n", indent, imports.add(Optional.class),
						imports.add(StreamProvider.class));
				p.format("%sreturn %s.createStream(contextPath, this);\n", indent.right(),
						imports.add(RequestHelper.class));
				p.format("%s}\n", indent.left());
			}

			// write Patched class
			writePatchAndPutMethods(t, simpleClassName, imports, indent, p);

			writeCopyMethod(t, simpleClassName, imports, indent, p, true);

			writeBoundActionMethods(t, typeActions, imports, indent, p, methodNames);

			writeBoundFunctionMethods(t, typeFunctions, imports, indent, p, methodNames);

			// write toString
			writeToString(t, simpleClassName, imports, indent, p);

			p.format("%s}\n", indent.left());

			writeToFile(imports, w, t.getClassFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void printSelectBuilder(Imports imports, Indent indent, PrintWriter p, List<TProperty> properties) {
		p.format("\n%sstatic abstract class SelectBuilderBase<T> {\n", indent);
		indent.right();
		
		// fields
		p.format("%sprivate final T caller;\n", indent);
		p.format("%sprotected final %s<%s> list = new %s<%s>();\n", indent, imports.add(List.class), imports.add(String.class), imports.add(ArrayList.class), imports.add(String.class));
		
		// constructor
		p.format("\n%sprotected SelectBuilderBase(T caller) {\n", indent);
		p.format("%sthis.caller = caller;\n", indent.right());
		p.format("%s}\n", indent.left());
		
		// methods
		for (TProperty t:properties) {
			String fieldName = Names.getIdentifier(t.getName());
	        p.format("%s\npublic SelectBuilderBase<T> %s() {\n" , indent, fieldName);
	        indent.right();
	        p.format("%slist.add(\"%s\");\n", indent, fieldName);
	        p.format("%sreturn this;\n", indent);
	        indent.left();
	        p.format("%s}\n", indent);
		}
		p.format("\n%spublic T build() {\n", indent);
		indent.right();
		p.format("%s return caller;\n", indent);
		p.format("%s}\n", indent.left());
		p.format("%s}\n", indent.left());
	}

	private static void printJsonIncludesNonNull(Indent indent, Imports imports, PrintWriter p) {
		p.format("%s@%s(%s.NON_NULL)\n", indent, imports.add(JsonInclude.class), imports.add(Include.class));
	}

	private void addInheritedPropertyNames(EntityType t, Set<String> methodNames) {
		while (t.hasBaseType()) {
			EntityType et = names.getEntityType(t.getBaseType());
			et.getProperties().forEach(p -> {
				methodNames.add(Names.getGetterMethod(p.getName()));
				methodNames.add(Names.getWithMethod(p.getName()));
				if (isStream(p)) {
					methodNames.add(Names.getPutChunkedMethod(p.getName()));
					methodNames.add(Names.getPutMethod(p.getName()));
				}
			});
			t = et;
		}
	}

	public static final int MAX_JAVADOC_WIDTH = 80;

	private void writeBoundActionMethods(EntityType t, Map<String, List<Action>> typeActions, Imports imports,
			Indent indent, PrintWriter p, Set<String> methodNames) {
		typeActions //
				.getOrDefault(t.getFullType(), Collections.emptyList()) //
				.forEach(action -> writeAction(imports, indent, p, action, methodNames));
	}

	private void writeAction(Imports imports, Indent indent, PrintWriter p, Action action, Set<String> methodNames) {
		p.format("\n%s@%s(name = \"%s\")\n", //
				indent, //
				imports.add(com.github.davidmoten.odata.client.annotation.Action.class), //
				action.getName());
		p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
		List<Parameter> parameters = action.getParametersUnbound(imports);
		String paramsDeclaration = parameters //
				.stream() //
				.map(x -> String.format("%s %s", x.importedFullClassName, x.nameJava())) //
				.collect(Collectors.joining(", "));
		String methodName = disambiguateMethodName(action.getActionMethodName(), methodNames, "_Action");
		// TODO add overload for actions with HttpRequestOptions parameter?
		if (action.hasReturnType()) {
			ReturnType returnType = action.getReturnType(imports);
			p.format("%spublic %s<%s> %s(%s) {\n", //
					indent, //
					returnType.isCollection ? imports.add(CollectionPageNonEntityRequest.class)
							: imports.add(ActionRequestReturningNonCollection.class), //
					action.getReturnType(imports).innerImportedFullClassName, methodName, paramsDeclaration);
			writeActionParameterMapAndNullChecksAndAsciiChecks(imports, indent, p, parameters);
			if (returnType.isCollection) {
				p.format(
						"%sreturn %s.forAction(this.contextPath.addActionOrFunctionSegment(\"%s\"), %s.class, _parameters, %s.INSTANCE);\n", //
						indent, //
						imports.add(CollectionPageNonEntityRequest.class), //
						action.getFullType(), //
						returnType.innerImportedFullClassName, //
						returnType.schemaInfoFullClassName);
			} else {
				p.format(
						"%sreturn new %s<%s>(this.contextPath.addActionOrFunctionSegment(\"%s\"), %s.class, _parameters, %s.INSTANCE);\n", //
						indent, //
						imports.add(ActionRequestReturningNonCollection.class), //
						returnType.innerImportedFullClassName, //
						action.getFullType(), //
						returnType.innerImportedFullClassName, //
						returnType.schemaInfoFullClassName);
			}
		} else {
			p.format("%spublic %s %s(%s) {\n", //
					indent, //
					imports.add(ActionRequestNoReturn.class), //
					methodName, paramsDeclaration);
			writeActionParameterMapAndNullChecksAndAsciiChecks(imports, indent, p, parameters);
			p.format("%sreturn new %s(this.contextPath.addActionOrFunctionSegment(\"%s\"), _parameters);\n", //
					indent, //
					imports.add(ActionRequestNoReturn.class), //
					action.getFullType(), //
					imports.add(HttpRequestOptions.class));
		}
		p.format("%s}\n", indent.left());
	}

	private static String disambiguateMethodName(String methodName, Set<String> methodNames, String suffix) {
		if (methodNames.contains(methodName)) {
			methodName = methodName + suffix;
		}
		while (methodNames.contains(methodName)) {
			methodName = methodName + "_";
		}
		methodNames.add(methodName);
		return methodName;
	}

	private void writeBoundFunctionMethods(EntityType t, Map<String, List<Function>> typeFunctions, Imports imports,
			Indent indent, PrintWriter p, Set<String> propertyMethodNames) {
		typeFunctions //
				.getOrDefault(t.getFullType(), Collections.emptyList()) //
				.forEach(function -> writeFunction(imports, indent, p, function, propertyMethodNames));
	}

	private void writeFunction(Imports imports, Indent indent, PrintWriter p, Function function,
			Set<String> methodNames) {
		p.format("\n%s@%s(name = \"%s\")\n", //
				indent, //
				imports.add(com.github.davidmoten.odata.client.annotation.Function.class), //
				function.getName());
		p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
		List<Function.Parameter> parameters = function.getParametersUnbound(imports);
		String paramsDeclaration = parameters //
				.stream() //
				.map(x -> String.format("%s %s", x.importedFullClassName, x.nameJava())) //
				.collect(Collectors.joining(", "));
		Function.ReturnType returnType = function.getReturnType(imports);
		String methodName = disambiguateMethodName(function.getActionMethodName(), methodNames, "_Function");
		p.format("%spublic %s<%s> %s(%s) {\n", //
				indent, //
				returnType.isCollection ? imports.add(CollectionPageNonEntityRequest.class)
						: imports.add(FunctionRequestReturningNonCollection.class), //
				function.getReturnType(imports).innerImportedFullClassName, methodName, paramsDeclaration);
		writeFunctionParameterMapAndNullChecksAndAsciiCheck(imports, indent, p, parameters);
		if (returnType.isCollection) {
			p.format(
					"%sreturn %s.forAction(this.contextPath.addActionOrFunctionSegment(\"%s\"), %s.class, _parameters, %s.INSTANCE);\n", //
					indent, //
					imports.add(CollectionPageNonEntityRequest.class), //
					function.getFullType(), //
					returnType.innerImportedFullClassName, //
					returnType.schemaInfoFullClassName);
		} else {
			p.format(
					"%sreturn new %s<%s>(this.contextPath.addActionOrFunctionSegment(\"%s\"), %s.class, _parameters, %s.INSTANCE);\n", //
					indent, //
					imports.add(FunctionRequestReturningNonCollection.class), //
					returnType.innerImportedFullClassName, //
					function.getFullType(), //
					returnType.innerImportedFullClassName, //
					returnType.schemaInfoFullClassName);
		}
		p.format("%s}\n", indent.left());
	}

	private static void writeActionParameterMapAndNullChecksAndAsciiChecks(Imports imports, Indent indent,
			PrintWriter p, List<Parameter> parameters) {
		indent.right();
		writeParameterNullChecks(imports, indent, p, parameters);
		p.format("%s%s<%s, %s> _parameters = %s%s;\n", //
				indent, //
				imports.add(Map.class), //
				imports.add(String.class), //
				imports.add(TypedObject.class), //
				imports.add(ParameterMap.class), //
				parameters.isEmpty() ? String.format(".empty()")
						: parameters //
								.stream() //
								.map(par -> formatParameterPut(imports, indent, par)) //
								.collect(Collectors.joining()) + "\n" + indent.copy().right() + ".build()");
	}

	private static String formatParameterPut(Imports imports, Indent indent, Parameter par) {
		final String expression;
		if (par.isAscii()) {
			expression = String.format("%s.checkIsAscii(%s)", imports.add(Checks.class), par.nameJava());
		} else {
			expression = par.nameJava();
		}
		return String.format("\n%s.put(\"%s\", \"%s\", %s)", //
				indent.copy().right(), //
				par.name, //
				par.typeWithNamespace, //
				expression);
	}

	private static String formatParameterPut(Imports imports, Indent indent, Function.Parameter par) {
		final String expression;
		if (par.isAscii()) {
			expression = String.format("%s.checkIsAscii(%s)", imports.add(Checks.class), par.nameJava());
		} else {
			expression = par.nameJava();
		}
		return String.format("\n%s.put(\"%s\", \"%s\", %s)", //
				indent.copy().right(), //
				par.name, //
				par.typeWithNamespace, //
				expression);
	}

	private static void writeParameterNullChecks(Imports imports, Indent indent, PrintWriter p,
			List<? extends HasNameJavaHasNullable> parameters) {
		parameters //
				.stream() //
				.filter(x -> !x.isNullable()) //
				.forEach(x -> p.format( //
						"%s%s.checkNotNull(%s, \"%s cannot be null\");\n", //
						indent, imports.add(Preconditions.class), //
						x.nameJava(), //
						x.nameJava()));
	}

	private static void writeFunctionParameterMapAndNullChecksAndAsciiCheck(Imports imports, Indent indent,
			PrintWriter p, List<Function.Parameter> parameters) {
		indent.right();
		writeParameterNullChecks(imports, indent, p, parameters);
		p.format("%s%s<%s, %s> _parameters = %s%s;\n", //
				indent, //
				imports.add(Map.class), //
				imports.add(String.class), //
				imports.add(TypedObject.class), //
				imports.add(ParameterMap.class), //
				parameters.isEmpty() ? String.format(".empty()")
						: parameters //
								.stream() //
								.map(par -> formatParameterPut(imports, indent, par)) //
								.collect(Collectors.joining()) + "\n" + indent.copy().right() + ".build()");
	}

	private void writeToString(Structure<?> t, String simpleClassName, Imports imports, Indent indent, PrintWriter p) {
		p.format("\n%s@%s\n", indent, imports.add(Override.class));
		p.format("%spublic %s toString() {\n", indent, imports.add(String.class));
		p.format("%s%s b = new %s();\n", indent.right(), imports.add(StringBuilder.class),
				imports.add(StringBuilder.class));
		p.format("%sb.append(\"%s[\");\n", indent, simpleClassName);
		boolean[] first = new boolean[1];
		first[0] = true;
		t.getFieldNames().stream().forEach(f -> {
			if (first[0]) {
				first[0] = false;
			} else {
				p.format("%sb.append(\", \");\n", indent);
			}
			p.format("%sb.append(\"%s=\");\n", indent, f.name);
			p.format("%sb.append(this.%s);\n", indent, f.fieldName);
		});
		p.format("%sb.append(\"]\");\n", indent);
		p.format("%sb.append(\",unmappedFields=\");\n", indent);
		p.format("%sb.append(unmappedFields);\n", indent);
		p.format("%sb.append(\",odataType=\");\n", indent);
		p.format("%sb.append(odataType);\n", indent);
		p.format("%sreturn b.toString();\n", indent);
		p.format("%s}\n", indent.left());
	}

	private void writeCopyMethod(Structure<?> t, String simpleClassName, Imports imports, Indent indent, PrintWriter p,
			boolean ofEntity) {
		List<FieldName> fields = t.getFieldNames();
		if (fields.isEmpty()) {
			// copy method not required if no fields to mutate on
			return;
		}
		p.format("\n%sprivate %s _copy() {\n", indent, simpleClassName);
		// use _x as identifier so doesn't conflict with any field name
		p.format("%s%s _x = new %s();\n", indent.right(), simpleClassName, simpleClassName);
		p.format("%s_x.contextPath = contextPath;\n", indent);
		if (ofEntity) {
			p.format("%s_x.changedFields = changedFields;\n", indent);
		}
		p.format("%s_x.unmappedFields = unmappedFields;\n", indent);
		p.format("%s_x.odataType = odataType;\n", //
				indent);
		fields.stream() //
				.map(f -> String.format("%s_x.%s = %s;\n", indent, f.fieldName, f.fieldName)) //
				.forEach(p::print);
		p.format("%sreturn _x;\n", indent);
		p.format("%s}\n", indent.left());
	}

	private void writeNoArgsConstructor(String simpleClassName, Indent indent, PrintWriter p, boolean hasBaseType) {
		p.format("\n%sprotected %s() {\n", indent, simpleClassName);
		indent.right();
		if (hasBaseType) {
			p.format("%ssuper();\n", indent);
		}
		p.format("%s}\n", indent.left());
	}

	private void writePatchAndPutMethods(EntityType t, String simpleClassName, Imports imports, Indent indent,
			PrintWriter p) {
		// write patch() method
		writePutOrPatchMethod(t, simpleClassName, imports, indent, p, true);

		// write put method
		writePutOrPatchMethod(t, simpleClassName, imports, indent, p, false);

	}

	private void writePutOrPatchMethod(EntityType t, String simpleClassName, Imports imports, Indent indent,
			PrintWriter p, boolean isPatch) {
		String methodName = isPatch ? "patch" : "put";
		if (isPatch) {
			p.format("\n%s/**", indent);
			p.format("\n%s * Submits only changed fields for update and returns an ", indent);
			p.format("\n%s * immutable copy of {@code this} with changed fields reset.", indent);
			p.format("\n%s *", indent);
			p.format("\n%s * @return a copy of {@code this} with changed fields reset", indent);
			p.format("\n%s * @throws %s if HTTP response is not as expected", indent,
					imports.add(ClientException.class));
			p.format("\n%s */", indent);
		} else {
			p.format("\n%s/**", indent);
			p.format("\n%s * Submits all fields for update and returns an immutable copy of {@code this}", indent);
			p.format("\n%s * with changed fields reset (they were ignored anyway).", indent);
			p.format("\n%s *", indent);
			p.format("\n%s * @return a copy of {@code this} with changed fields reset", indent);
			p.format("\n%s * @throws %s if HTTP response is not as expected", indent,
					imports.add(ClientException.class));
			p.format("\n%s */", indent);
		}
		p.format("\n%spublic %s %s() {\n", indent, simpleClassName, methodName);
		p.format("%s%s.%s(this, contextPath, %s.EMPTY);\n", indent.right(), imports.add(RequestHelper.class),
				methodName, imports.add(RequestOptions.class));

		// use _x as identifier so doesn't conflict with any field name
		p.format("%s%s _x = _copy();\n", indent, simpleClassName);
		p.format("%s_x.changedFields = null;\n", indent);
		p.format("%sreturn _x;\n", indent);
		p.format("%s}\n", indent.left());
	}

	private static void addChangedFieldsField(Imports imports, Indent indent, PrintWriter p) {
		p.format("\n%s@%s\n", indent, imports.add(JacksonInject.class));
		p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
		p.format("%sprotected %s changedFields;\n", indent, imports.add(ChangedFields.class));
	}

	private void writeToFile(Imports imports, StringWriter w, File classFile) throws IOException {
		byte[] bytes = w //
				.toString() //
				.replace("IMPORTSHERE", imports.toString()) //
				.getBytes(StandardCharsets.UTF_8);
		Files.write(classFile.toPath(), bytes);
	}

	private void writeComplexType(Schema schema, TComplexType complexType) {
		ComplexType t = new ComplexType(complexType, names);
		t.getDirectoryComplexType().mkdirs();
		String simpleClassName = t.getSimpleClassName();
		Imports imports = new Imports(t.getFullClassName());
		Indent indent = new Indent();

		StringWriter w = new StringWriter();
		try (PrintWriter p = new PrintWriter(w)) {
			p.format("package %s;\n\n", t.getPackage());
			p.format("IMPORTSHERE");

			t.printJavadoc(p, indent);
			printPropertyOrder(imports, p, t.getProperties());
			printJsonIncludesNonNull(indent, imports, p);
			p.format("public class %s%s implements %s {\n\n", simpleClassName, t.getExtendsClause(imports),
					imports.add(ODataType.class));

			indent.right();
			if (!t.hasBaseType()) {
				addContextPathField(imports, indent, p);
			}

			addUnmappedFieldsField(imports, indent, p);

			// write fields from properties
			printPropertyFields(imports, indent, p, t.getProperties(), t.hasBaseType());

			// write constructor
			writeNoArgsConstructor(simpleClassName, indent, p, t.hasBaseType());

			p.format("\n%s@%s\n", indent, imports.add(Override.class));
			p.format("%spublic String odataTypeName() {\n", indent);
			p.format("%sreturn \"%s\";\n", indent.right(), t.getFullType());
			p.format("%s}\n", indent.left());

			Set<String> methodNames = new HashSet<>();
			printPropertyGetterAndSetters(t, imports, indent, p, simpleClassName, t.getFullType(), t.getProperties(),
					false, methodNames);

			addUnmappedFieldsSetterAndGetter(imports, indent, p, methodNames);

			p.format("\n%s@%s\n", indent, imports.add(Override.class));
			p.format("%spublic void postInject(boolean addKeysToContextPath) {\n", indent);
			p.format("%s// do nothing;\n", indent.right());
			p.format("%s}\n", indent.left());

			writeBuilder(t, simpleClassName, imports, indent, p);

			// write copy method
			if (t.getProperties() //
					.stream() //
					.filter(x -> !isCollection(x)) //
					.filter(x -> !isStream(x)) //
					.findAny() //
					.isPresent()) {
				writeCopyMethod(t, simpleClassName, imports, indent, p, false);
			}

			// write toString
			writeToString(t, simpleClassName, imports, indent, p);

			p.format("\n}\n");
			writeToFile(imports, w, t.getClassFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeEntityRequest(Schema schema, TEntityType entityType, Map<String, List<Action>> typeActions,
			Map<String, List<Function>> typeFunctions) {
		EntityType t = new EntityType(entityType, names);
		names.getDirectoryEntityRequest(schema).mkdirs();
		// TODO only write out those requests needed
		String simpleClassName = t.getSimpleClassNameEntityRequest();
		Imports imports = new Imports(t.getFullClassNameEntityRequest());
		Indent indent = new Indent();

		StringWriter w = new StringWriter();
		try (PrintWriter p = new PrintWriter(w)) {
			p.format("package %s;\n\n", t.getPackageEntityRequest());
			p.format("IMPORTSHERE");

			p.format("@%s\n", imports.add(JsonIgnoreType.class));
			// don't make class final because can get extended by EntitySet
			p.format("public class %s extends %s {\n\n", simpleClassName,
					imports.add(EntityRequest.class) + "<" + imports.add(t.getFullClassNameEntity()) + ">");

			indent.right();

			// add constructor
			p.format("%spublic %s(%s contextPath) {\n", indent, simpleClassName, imports.add(ContextPath.class),
					imports.add(String.class));
			p.format("%ssuper(%s.class, contextPath, %s.INSTANCE);\n", //
					indent.right(), //
					imports.add(t.getFullClassNameEntity()), //
					imports.add(names.getFullClassNameSchemaInfo(schema)));
			p.format("%s}\n", indent.left());

			indent.left();

			// TODO also support navigation properties with complexTypes?
			t.getNavigationProperties() //
					.stream() //
					.filter(x -> {
						boolean isEntity = names.isEntityWithNamespace(names.getInnerType(names.getType(x)));
						if (!isEntity) {
							log("Unexpected entity with non-entity navigation property type: " + simpleClassName + "."
									+ x.getName()
									+ ". If you get this message then raise an issue on the github project for odata-client.");
						}
						return isEntity;
					}) //
					.forEach(x -> {
						indent.right();
						final String returnClass;
						String y = x.getType().get(0);
						Schema sch = names.getSchema(names.getInnerType(y));
						if (Names.isCollection(y)) {
							returnClass = toClassName(x, imports);
						} else {
							returnClass = imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, y));
						}
						p.format("\n%spublic %s %s() {\n", //
								indent, //
								returnClass, //
								Names.getGetterMethodWithoutGet(x.getName()));
						if (isCollection(x)) {
							p.format("%sreturn new %s(\n", indent.right(), toClassName(x, imports));
							p.format("%scontextPath.addSegment(\"%s\"));\n", indent.right().right().right().right(),
									x.getName());
							indent.left().left().left().left();
						} else {
							p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", indent.right(), returnClass,
									x.getName());
						}
						p.format("%s}\n", indent.left());

						// if collection then add with id method
						if (y.startsWith(COLLECTION_PREFIX)) {
							// TODO use actual key name from metadata
							String inner = names.getInnerType(y);
							// TODO remove redundant check
							if (names.isEntityWithNamespace(inner)) {
								String entityRequestType = names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
										inner);
								EntityType et = names.getEntityType(inner);
								KeyInfo k = getKeyInfo(et, imports);

								p.format("\n%spublic %s %s(%s) {\n", indent, imports.add(entityRequestType),
										Names.getIdentifier(x.getName()), k.typedParams);
								p.format("%sreturn new %s(contextPath.addSegment(\"%s\")%s);\n", indent.right(),
										imports.add(entityRequestType), x.getName(), k.addKeys);
								p.format("%s}\n", indent.left());
							}
						}
						indent.left();
					});
			indent.right();
			Set<String> methodNames = new HashSet<>();
			writeBoundActionMethods(t, typeActions, imports, indent, p, methodNames);
			writeBoundFunctionMethods(t, typeFunctions, imports, indent, p, methodNames);
			indent.left();
			p.format("\n}\n");
			writeToFile(imports, w, t.getClassFileEntityRequest());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final class KeyInfo {
		final String typedParams;
		final String addKeys;

		KeyInfo(String typedParams, String addKeys) {
			this.typedParams = typedParams;
			this.addKeys = addKeys;
		}
	}

	private static List<String> fieldNames(EntityType et) {
		return fieldNames(et.getFirstKey());
	}

	private static List<String> fieldNames(KeyElement key) {
		return key //
				.getPropertyRefs() //
				.stream() //
				.map(z -> z.getReferredProperty().getFieldName()) //
				.collect(Collectors.toList());
	}

	private KeyInfo getKeyInfo(EntityType et, Imports imports) {
		KeyElement key = et.getFirstKey();

		String typedParams = key //
				.getPropertyRefs() //
				.stream() //
				.map(z -> z.getReferredProperty()) //
				.map(z -> String.format("%s %s", z.getImportedType(imports), z.getFieldName())) //
				.collect(Collectors.joining(", "));

		String addKeys = getAddKeys(et, imports, key);
		return new KeyInfo(typedParams, addKeys);
	}

	private String getAddKeys(EntityType et, Imports imports) {
		KeyElement key = et.getFirstKey();
		return getAddKeys(et, imports, key);
	}

	private String getAddKeys(EntityType et, Imports imports, KeyElement key) {
		String addKeys = et.getFirstKey() //
				.getPropertyRefs() //
				.stream() //
				.map(z -> z.getReferredProperty()) //
				.map(z -> {
					if (key.getPropertyRefs().size() > 1) {
						return String.format("new %s(\"%s\", %s)", imports.add(NameValue.class), z.getName(),
								z.getFieldName());
					} else {
						return String.format("new %s(%s.toString())", imports.add(NameValue.class), z.getFieldName());
					}
				}) //
				.collect(Collectors.joining(", "));
		return ".addKeys(" + addKeys + ")";
	}

	private void writeContainer(Schema schema, TEntityContainer t) {
		names.getDirectoryContainer(schema).mkdirs();
		String simpleClassName = names.getSimpleClassNameContainer(schema, t.getName());
		Imports imports = new Imports(names.getFullClassNameContainer(schema, t.getName()));
		Indent indent = new Indent();

		StringWriter w = new StringWriter();
		try (PrintWriter p = new PrintWriter(w)) {
			p.format("package %s;\n\n", names.getPackageContainer(schema));
			p.format("IMPORTSHERE");

			final String extension;
			if (t.getExtends() != null) {
				extension = " extends " + imports.add(names.getFullClassNameFromTypeWithNamespace(t.getExtends()));
			} else {
				extension = "";
			}
			p.format("public final class %s%s implements %s {\n\n", simpleClassName, extension,
					imports.add(HasContext.class));

			// TODO handle container extension

			// write fields
			p.format("%sprivate final %s contextPath;\n\n", indent.right(), imports.add(ContextPath.class));

			// write constructor
			p.format("%spublic %s(%s context) {\n", indent, simpleClassName, imports.add(Context.class));
			p.format("%sthis.contextPath = new %s(context, context.service().getBasePath());\n", indent.right(),
					imports.add(ContextPath.class));
			p.format("%s}\n", indent.left());

			p.format("\n%s@%s\n", indent, imports.add(Override.class));
			p.format("%spublic %s _context() {\n", indent, imports.add(Context.class));
			p.format("%sreturn contextPath.context();\n", indent.right());
			p.format("%s}\n", indent.left());

			p.format("\n%spublic %s _service() {\n", indent, imports.add(HttpService.class));
			p.format("%sreturn contextPath.context().service();\n", indent.right());
			p.format("%s}\n", indent.left());

			// write static testing method
			p.format("\n%sstatic final class ContainerBuilderImpl extends %s<%s> {\n", indent,
					imports.add(ContainerBuilder.class), simpleClassName);
			p.format("\n%s@%s\n", indent.right(), imports.add(Override.class));
			p.format("%spublic %s _create(%s context) {\n", indent, simpleClassName, imports.add(Context.class));
			p.format("%sreturn new %s(context);\n", indent.right(), simpleClassName);
			p.format("%s}\n", indent.left());
			p.format("%s}\n", indent.left());

			p.format("\n%spublic static %s<%s<%s>, %s> test() {\n", indent, imports.add(BuilderBase.class),
					imports.add(ContainerBuilder.class), simpleClassName, simpleClassName);
			p.format("%sreturn new ContainerBuilderImpl();\n", indent.right());
			p.format("%s}\n", indent.left());

			// write get methods from properties
			Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
					.forEach(x -> {
						EntitySet es = new EntitySet(schema, t, x, names);
						Schema sch = names.getSchema(x.getEntityType());
						p.format("\n%spublic %s %s() {\n", indent, imports.add(es.getFullClassNameEntitySet()),
								Names.getIdentifier(x.getName()));
						p.format("%sreturn new %s(\n", indent.right(), imports.add(es.getFullClassNameEntitySet()));
						p.format("%scontextPath.addSegment(\"%s\"));\n", indent.right().right().right().right(),
								x.getName());
						p.format("%s}\n", indent.left().left().left().left().left());

						if (names.isEntityWithNamespace(x.getEntityType())) {
							String entityRequestType = names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
									x.getEntityType());

							EntityType et = names.getEntityType(x.getEntityType());
							KeyInfo k = getKeyInfo(et, imports);

							p.format("\n%spublic %s %s(%s) {\n", indent, imports.add(entityRequestType),
									Names.getIdentifier(x.getName()), k.typedParams);
							p.format("%sreturn new %s(contextPath.addSegment(\"%s\")%s);\n", indent.right(),
									imports.add(entityRequestType), x.getName(), k.addKeys);
							p.format("%s}\n", indent.left());
						}
					});

			Util //
					.filter(t.getEntitySetOrActionImportOrFunctionImport(), TSingleton.class) //
					.forEach(x -> {
						String importedType = toClassName(x, imports);
						p.format("\n%spublic %s %s() {\n", indent, importedType, Names.getIdentifier(x.getName()));
						p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", indent.right(), importedType,
								x.getName());
						p.format("%s}\n", indent.left());
					});

			// write unbound actions
			Set<String> methodNames = new HashSet<>();
			Util //
					.types(schema, TAction.class) //
					.filter(x -> !x.isIsBound())
					.forEach(x -> writeAction(imports, indent, p, new Action(x, names), methodNames));

			Util //
					.types(schema, TFunction.class) //
					.filter(x -> !x.isIsBound())
					.forEach(x -> writeFunction(imports, indent, p, new Function(x, names), methodNames));

			p.format("\n}\n");
			File classFile = names.getClassFileContainer(schema, t.getName());
			writeToFile(imports, w, classFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeEntityCollectionRequest(Schema schema, TEntityType entityType,
			Map<String, List<Action>> collectionTypeActions, Map<String, List<Function>> collectionTypeFunctions,
			Set<String> collectionTypes) {
		EntityType t = new EntityType(entityType, names);
		if (!collectionTypes.contains(t.getFullType())) {
			return;
		}
		names.getDirectoryEntityCollectionRequest(schema).mkdirs();
		String simpleClassName = names.getSimpleClassNameCollectionRequest(schema, t.getName());
		Imports imports = new Imports(names.getFullClassNameCollectionRequest(schema, t.getName()));
		Indent indent = new Indent();

		StringWriter w = new StringWriter();
		try (PrintWriter p = new PrintWriter(w)) {
			p.format("package %s;\n\n", names.getPackageCollectionRequest(schema));
			p.format("IMPORTSHERE");
			p.format("public class %s extends %s<%s, %s>{\n\n", simpleClassName,
					imports.add(CollectionPageEntityRequest.class),
					imports.add(names.getFullClassNameFromTypeWithoutNamespace(schema, t.getName())), //
					imports.add(names.getFullClassNameEntityRequest(schema, t.getName())));

			indent.right();
			addContextPathField(imports, indent, p);

			// add constructor
			p.format("\n%spublic %s(%s contextPath) {\n", indent, simpleClassName, imports.add(ContextPath.class),
					imports.add(String.class));
			p.format("%ssuper(contextPath, %s.class, cp -> new %s(cp), %s.INSTANCE);\n", indent.right(),
					imports.add(names.getFullClassNameFromTypeWithoutNamespace(schema, t.getName())), //
					imports.add(names.getFullClassNameEntityRequestFromTypeWithoutNamespace(schema, t.getName())), //
					imports.add(names.getFullClassNameSchemaInfo(schema)));
			p.format("%sthis.contextPath = contextPath;\n", indent);
			p.format("%s}\n", indent.left());

			// write fields from properties
			t.getNavigationProperties() //
					.stream() //
					.forEach(x -> {
						Schema sch = names.getSchema(names.getInnerType(names.getType(x)));
						p.println();
						if (x.getType().get(0).startsWith(COLLECTION_PREFIX)) {
							String y = names.getInnerType(names.getType(x));
							p.format("%spublic %s %s() {\n", //
									indent, //
									imports.add(names.getFullClassNameCollectionRequestFromTypeWithNamespace(sch, y)), //
									Names.getIdentifier(x.getName()));

							p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", //
									indent.right(), //
									imports.add(names.getFullClassNameCollectionRequestFromTypeWithNamespace(sch, y)), //
									x.getName());
							p.format("%s}\n", indent.left());

							if (names.isEntityWithNamespace(y)) {
								String entityRequestType = names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
										y);
								EntityType et = names.getEntityType(y);
								KeyInfo k = getKeyInfo(et, imports);

								p.format("\n%spublic %s %s(%s) {\n", indent, imports.add(entityRequestType),
										Names.getIdentifier(x.getName()), k.typedParams);
								p.format("%sreturn new %s(contextPath.addSegment(\"%s\")%s);\n", indent.right(),
										imports.add(entityRequestType), x.getName(), k.addKeys);
								p.format("%s}\n", indent.left());
							}
						}
					});

			Set<String> methodNames = new HashSet<>();
			writeBoundActionMethods(t, collectionTypeActions, imports, indent, p, methodNames);
			writeBoundFunctionMethods(t, collectionTypeFunctions, imports, indent, p, methodNames);

			indent.left();
			p.format("\n}\n");
			writeToFile(imports, w, t.getClassFileCollectionRequest());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void writeBuilder(Structure<?> t, String simpleClassName, Imports imports, Indent indent, PrintWriter p) {
		if (t.isAbstract()) {
			return;
		}
		String builderSuffix = t.getBaseType() == null ? "" : simpleClassName;

		p.format("\n%s/**", indent);
		p.format("\n%s * Returns a builder which is used to create a new", indent);
		p.format("\n%s * instance of this class (given that this class is immutable).", indent);
		p.format("\n%s *", indent);
		p.format("\n%s * @return a new Builder for this class", indent);
		p.format("\n%s */", indent);
		p.format("\n%s// Suffix used on builder factory method to differentiate the method", indent);
		p.format("\n%s// from static builder methods on superclasses", indent);
		p.format("\n%spublic static Builder builder%s() {\n", indent, builderSuffix);
		p.format("%sreturn new Builder();\n", indent.right());
		p.format("%s}\n", indent.left());

		// write Builder class
		p.format("\n%spublic static final class Builder {\n", indent);
		indent.right();
		List<Field> fields = t.getFields(imports);

		// write builder fields
		fields //
				.forEach(f -> {
					p.format("%sprivate %s %s;\n", indent, f.importedType, f.fieldName);
				});
		if (!fields.isEmpty()) {
			p.format("%sprivate %s changedFields = new %s();\n", indent, imports.add(ChangedFields.class),
					imports.add(ChangedFields.class));
		}

		p.format("\n%sBuilder() {\n", indent);
		p.format("%s// prevent instantiation\n", indent.right());
		p.format("%s}\n", indent.left());

		// write builder setters

		fields.forEach(f -> {
			Map<String, String> map = new LinkedHashMap<>();
			map.put(f.fieldName, "value of {@code " + f.propertyName + "} property (as defined in service metadata)");
			t.printPropertyJavadoc(p, indent, f.name, "{@code this} (for method chaining)", map);
			p.format("\n%spublic Builder %s(%s %s) {\n", indent, f.fieldName, f.importedType, f.fieldName);
			p.format("%sthis.%s = %s;\n", indent.right(), f.fieldName, f.fieldName);
			p.format("%sthis.changedFields = changedFields.add(\"%s\");\n", indent, f.name);
			p.format("%sreturn this;\n", indent);
			p.format("%s}\n", indent.left());
			if (f.isCollection) {
				// add overload with T... parameters instead of List<T>
				t.printPropertyJavadoc(p, indent, f.name, "{@code this} (for method chaining)", map);
				p.format("\n%spublic Builder %s(%s... %s) {\n", indent, f.fieldName, imports.add(f.innerFullClassName),
						f.fieldName);
				p.format("%sreturn %s(%s.asList(%s));\n", indent.right(), f.fieldName, imports.add(Arrays.class),
						f.fieldName);
				p.format("%s}\n", indent.left());
			}
		});

		p.format("\n%spublic %s build() {\n", indent, simpleClassName);
		// use _x as identifier so doesn't conflict with any field name
		p.format("%s%s _x = new %s();\n", indent.right(), simpleClassName, simpleClassName);
		p.format("%s_x.contextPath = null;\n", indent);
		if (t instanceof EntityType) {
			p.format("%s_x.changedFields = changedFields;\n", indent);
		}
		p.format("%s_x.unmappedFields = new %s();\n", indent, imports.add(UnmappedFields.class));
		p.format("%s_x.odataType = \"%s\";\n", indent, t.getFullType());
		fields.stream().map(f -> String.format("%s_x.%s = %s;\n", indent, f.fieldName, f.fieldName)).forEach(p::print);
		p.format("%sreturn _x;\n", indent);
		p.format("%s}\n", indent.left());

		p.format("%s}\n", indent.left());
	}

	private static void addUnmappedFieldsField(Imports imports, Indent indent, PrintWriter p) {
		p.format("\n%s@%s\n", indent, imports.add(JacksonInject.class));
		p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
		p.format("%sprotected %s unmappedFields;\n", indent, imports.add(UnmappedFields.class));
	}

	private static void addUnmappedFieldsSetterAndGetter(Imports imports, Indent indent, PrintWriter p,
			Set<String> methodNames) {
		p.format("\n%s@%s\n", indent, imports.add(JsonAnySetter.class));
		// TODO protect "setUnmappedField" name against clashes
		methodNames.add("setUnmappedField");
		p.format("%sprivate void setUnmappedField(String name, Object value) {\n", indent);
		p.format("%sif (unmappedFields == null) {\n", indent.right());
		p.format("%sunmappedFields = new %s();\n", indent.right(), imports.add(UnmappedFields.class));
		p.format("%s}\n", indent.left());
		p.format("%sunmappedFields.put(name, value);\n", indent);
		p.format("%s}\n", indent.left());

		methodNames.add("getUnmappedField");
		p.format("\n%s@%s\n", indent, imports.add(Override.class));
		p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
		p.format("%spublic %s getUnmappedFields() {\n", indent, imports.add(UnmappedFields.class));
		p.format("%sreturn unmappedFields == null ? new %s() : unmappedFields;\n", indent.right(),
				imports.add(UnmappedFields.class));
		p.format("%s}\n", indent.left());
	}

	private static void addContextPathInjectableField(Imports imports, Indent indent, PrintWriter p) {
		// add context path field
		p.format("\n%s@%s\n", indent, imports.add(JacksonInject.class));
		p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
		addContextPathField(imports, indent, p);
	}

	private static void addContextPathField(Imports imports, Indent indent, PrintWriter p) {
		p.format("%sprotected %s%s contextPath;\n", indent, "", imports.add(ContextPath.class));
	}

	private void printPropertyGetterAndSetters(Structure<?> structure, Imports imports, Indent indent, PrintWriter p,
			String simpleClassName, String fullType, List<TProperty> properties, boolean ofEntity,
			Set<String> methodNames) {

		// write getters and setters
		properties //
				.forEach(x -> {
					String fieldName = Names.getIdentifier(x.getName());
					String t = names.getType(x);
					boolean isCollection = isCollection(x);
					structure.printPropertyJavadoc(p, indent, x.getName(), "property " + x.getName(),
							Collections.emptyMap());
					addPropertyAnnotation(imports, indent, p, x.getName());
					p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
					String methodName = Names.getGetterMethod(x.getName());
					methodNames.add(methodName);
					if (isCollection) {
						String inner = names.getInnerType(t);
						String importedInnerType = names.toImportedTypeNonCollection(inner, imports);
						boolean isEntity = names.isEntityWithNamespace(inner);
						{
							String options = String.format("%s.EMPTY", imports.add(HttpRequestOptions.class));
							p.format("%spublic %s<%s> %s() {\n", indent, imports.add(CollectionPage.class),
									importedInnerType, methodName);
							writePropertyGetterCollectionBody(imports, indent, p, fieldName, inner, importedInnerType,
									isEntity, options);
							p.format("%s}\n", indent.left());
						}
						{
							String options = "options";
							structure.printPropertyJavadoc(p, indent, x.getName(), "property " + x.getName(),
									Collections.emptyMap());
							addPropertyAnnotation(imports, indent, p, x.getName());
							p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
							
							p.format("%spublic %s<%s> %s(%s options) {\n", indent, imports.add(CollectionPage.class),
									importedInnerType, methodName, imports.add(HttpRequestOptions.class));
							writePropertyGetterCollectionBody(imports, indent, p, fieldName, inner, importedInnerType,
									isEntity, options);
							p.format("%s}\n", indent.left());
						}
					} else {
						boolean isStream = isStream(x);
						if (isStream) {
							p.format("%spublic %s<%s> %s() {\n", indent, imports.add(Optional.class),
									imports.add(StreamProvider.class), methodName);
							p.format("%sreturn %s.createStreamForEdmStream(contextPath, this, \"%s\", %s);\n",
									indent.right(), imports.add(RequestHelper.class), x.getName(), fieldName);
							p.format("%s}\n", indent.left());

							String putMethodName = Names.getPutMethod(x.getName());
							methodNames.add(putMethodName);
							p.format("\n%s/**", indent);
							p.format("\n%s * If metadata indicate that the stream is editable then returns", indent);
							p.format("\n%s * a {@link StreamUploader} which can be used to upload the stream", indent);
							p.format("\n%s * to the {@code %s} property.", indent, x.getName());
							p.format("\n%s *", indent);
							p.format("\n%s * @return a StreamUploader if upload permitted", indent);
							p.format("\n%s */", indent);
							addPropertyAnnotation(imports, indent, p, x.getName());
							p.format("\n%spublic %s<%s> %s() {\n", indent, imports.add(Optional.class), //
									imports.add(StreamUploader.class), //
									putMethodName);
							p.format("%sreturn %s(%s.singleCall());\n", //
									indent.right(), //
									putMethodName, imports.add(UploadStrategy.class) //
							);
							p.format("%s}\n", indent.left());

							String putChunkedMethodName = Names.getPutChunkedMethod(x.getName());
							methodNames.add(putChunkedMethodName);
							p.format("\n%s/**", indent);
							p.format("\n%s * If metadata indicate that the stream is editable then returns", indent);
							p.format("\n%s * a {@link StreamUploaderChunked} which can be used to upload the stream",
									indent);
							p.format("\n%s * to the {@code %s} property.", indent, x.getName());
							p.format("\n%s *", indent);
							p.format("\n%s * @return a StreamUploaderChunked if upload permitted", indent);
							p.format("\n%s */", indent);
							addPropertyAnnotation(imports, indent, p, x.getName());
							p.format("\n%spublic %s<%s> %s() {\n", indent, imports.add(Optional.class), //
									imports.add(StreamUploaderChunked.class), //
									putChunkedMethodName);
							p.format("%sreturn %s(%s.chunked());\n", //
									indent.right(), //
									putMethodName, imports.add(UploadStrategy.class) //
							);
							p.format("%s}\n", indent.left());

							addPropertyAnnotation(imports, indent, p, x.getName());
							p.format("\n%spublic <T> T %s(%s<T> strategy) {\n", //
									indent, //
									putMethodName, //
									imports.add(UploadStrategy.class));
							p.format("%sreturn strategy.builder(contextPath, this, \"%s\");\n", //
									indent.right(), //
									x.getName());
							p.format("%s}\n", indent.left());
						} else {
							final String importedType = names.toImportedTypeNonCollection(t, imports);
							String importedTypeWithOptional = imports.add(Optional.class) + "<" + importedType + ">";
							p.format("%spublic %s %s() {\n", indent, importedTypeWithOptional, methodName);
							p.format("%sreturn %s.ofNullable(%s);\n", indent.right(), imports.add(Optional.class),
									fieldName);
							p.format("%s}\n", indent.left());

							Map<String, String> map = new LinkedHashMap<>();
							map.put(fieldName,
									"new value of {@code " + x.getName() + "} field (as defined in service metadata)");
							structure.printMutatePropertyJavadoc(p, indent, x.getName(), map);
							String classSuffix = "";
							String withMethodName = Names.getWithMethod(x.getName());
							methodNames.add(withMethodName);
							p.format("\n%spublic %s%s %s(%s %s) {\n", indent, simpleClassName, classSuffix,
									withMethodName, importedType, fieldName);
							if (x.isUnicode() != null && !x.isUnicode()) {
								p.format("%s%s.checkIsAscii(%s);\n", indent.right(), imports.add(Checks.class),
										fieldName, fieldName);
								indent.left();
							}
							// use _x as identifier so doesn't conflict with any field name
							p.format("%s%s _x = _copy();\n", indent.right(), simpleClassName, simpleClassName);
							if (ofEntity) {
								p.format("%s_x.changedFields = changedFields.add(\"%s\");\n", indent, x.getName());
							}
							p.format("%s_x.odataType = %s.nvl(odataType, \"%s\");\n", //
									indent, //
									imports.add(com.github.davidmoten.odata.client.Util.class), //
									fullType);
							p.format("%s_x.%s = %s;\n", indent, fieldName, fieldName);
							p.format("%sreturn _x;\n", indent);
							p.format("%s}\n", indent.left());
						}
					}

				});
	}

	private void writePropertyGetterCollectionBody(Imports imports, Indent indent, PrintWriter p, String fieldName,
			String inner, String importedInnerType, boolean isEntity, String options) {
		if (isEntity) {
			Schema sch = names.getSchema(inner);
			p.format("%sreturn %s.from(contextPath.context(), %s, %s.class, %s.INSTANCE, %s.emptyList());\n",
					indent.right(), imports.add(CollectionPage.class), fieldName, importedInnerType,
					imports.add(names.getFullClassNameSchemaInfo(sch)), //
					imports.add(Collections.class));
		} else {
			final String importedSchemaInfo;
			if (inner.startsWith("Edm.")) {
				importedSchemaInfo = imports.add(EdmSchemaInfo.class);
			} else {
				Schema sch = names.getSchema(inner);
				importedSchemaInfo = imports.add(names.getFullClassNameSchemaInfo(sch));
			}
			p.format(
					"%sreturn new %s<%s>(contextPath, %s.class, %s, %s.ofNullable(%sNextLink), %s.INSTANCE, %s.emptyList(), %s);\n",
					indent.right(), imports.add(CollectionPage.class), importedInnerType, importedInnerType, fieldName,
					imports.add(Optional.class), fieldName, importedSchemaInfo, imports.add(Collections.class), //
					options);
		}
	}

	private boolean isStream(TProperty x) {
		return "Edm.Stream".equals(names.getType(x));
	}

	private void addPropertyAnnotation(Imports imports, Indent indent, PrintWriter p, String name) {
		p.format("\n%s@%s(name=\"%s\")", indent, imports.add(Property.class), name);
	}

	private void addNavigationPropertyAnnotation(Imports imports, Indent indent, PrintWriter p, String name) {
		p.format("\n%s@%s(name=\"%s\")\n", indent, imports.add(NavigationProperty.class), name);
	}

	private void printPropertyOrder(Imports imports, PrintWriter p, List<TProperty> properties) {
		String props = Stream.concat( //
				Stream.of("@odata.type"), properties.stream().map(x -> x.getName())) //
				.map(x -> "\n    \"" + x + "\"") //
				.collect(Collectors.joining(", "));
		p.format("@%s({%s})\n", imports.add(JsonPropertyOrder.class), props);
	}

	private void printPropertyFields(Imports imports, Indent indent, PrintWriter p, List<TProperty> properties,
			boolean hasBaseType) {

		// TODO make a wrapper for TProperty that passes propertyName, fieldName,
		// importedType
		if (!hasBaseType) {
			p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), "@odata.type");
			p.format("%sprotected %s %s;\n", indent, imports.add(String.class), "odataType");
		}
		properties.stream().forEach(x -> {
			p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), x.getName());
			p.format("%sprotected %s %s;\n", indent, names.toImportedFullClassName(x, imports),
					Names.getIdentifier(x.getName()));
			String t = names.getInnerType(names.getType(x));
			if (isCollection(x) && !names.isEntityWithNamespace(t)) {
				p.format("\n%s@%s(\"%s@nextLink\")\n", indent, imports.add(JsonProperty.class), x.getName());
				p.format("%sprotected %s %sNextLink;\n", indent, imports.add(String.class),
						Names.getIdentifier(x.getName()));
			}
		});
	}

	private void printNavigationPropertyGetters(Structure<?> structure, Imports imports, Indent indent, PrintWriter p,
			List<TNavigationProperty> properties, Set<String> methodNames) {
		// write getters
		properties //
				.stream() //
				.forEach(x -> {
					String typeName = toClassName(x, imports);
					String methodName = Names.getGetterMethod(x.getName());
					methodNames.add(methodName);
					structure.printPropertyJavadoc(p, indent, x.getName(), "navigational property " + x.getName(),
							Collections.emptyMap());
					addNavigationPropertyAnnotation(imports, indent, p, x.getName());
					p.format("%s@%s\n", indent, imports.add(JsonIgnore.class));
					p.format("%spublic %s %s() {\n", indent, typeName, methodName);
					if (isCollection(x)) {
						if (names.isEntityWithNamespace(names.getType(x))) {
							p.format("%sreturn new %s(\n", indent.right(), toClassName(x, imports));
							p.format("%scontextPath.addSegment(\"%s\"));\n", //
									indent.right().right().right().right(), x.getName());
							indent.left().left().left().left();
						} else {
							throw new RuntimeException("unexpected");
						}
					} else {
						if (names.isEntityWithNamespace(names.getType(x))) {
							Schema sch = names.getSchema(names.getInnerType(names.getType(x)));
							p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", //
									indent.right(), //
									imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
											names.getInnerType(names.getType(x)))),
									x.getName());
						} else {
							throw new RuntimeException("unexpected");
						}
					}
					p.format("%s}\n", indent.left());
				});
	}

	private String toClassName(TNavigationProperty x, Imports imports) {
		Preconditions.checkArgument(x.getType().size() == 1);
		String t = x.getType().get(0);
		if (!isCollection(x)) {
			if (x.isNullable() != null && x.isNullable()) {
				String r = names.toImportedFullClassName(t, imports, List.class);
				return imports.add(Optional.class) + "<" + r + ">";
			} else {
				// is navigation property so must be an entity and is a single request
				Schema sch = names.getSchema(names.getInnerType(t));
				return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, t));
			}
		} else {
			String inner = names.getInnerType(t);
			Schema schema = names.getSchema(inner);
			return imports.add(names.getFullClassNameCollectionRequestFromTypeWithNamespace(schema, inner));
		}
	}

	private String toClassName(TSingleton x, Imports imports) {
		String t = x.getType();
		if (!isCollection(x.getType())) {
			Schema sch = names.getSchema(names.getInnerType(t));
			return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, t));
		} else {
			return names.toImportedFullClassName(t, imports, CollectionPageEntityRequest.class);
		}
	}

	private boolean isCollection(TProperty x) {
		return isCollection(names.getType(x));
	}

	private boolean isCollection(TNavigationProperty x) {
		return isCollection(names.getType(x));
	}

	private static boolean isCollection(String t) {
		return t.startsWith(COLLECTION_PREFIX) && t.endsWith(")");
	}

}
