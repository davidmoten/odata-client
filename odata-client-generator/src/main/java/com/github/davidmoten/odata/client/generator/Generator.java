package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityContainer;
import org.oasisopen.odata.csdl.v4.TEntitySet;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;
import org.oasisopen.odata.csdl.v4.TFunction;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
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
import com.github.davidmoten.odata.client.ActionFunctionRequestReturningCollection;
import com.github.davidmoten.odata.client.ActionFunctionRequestReturningNonCollection;
import com.github.davidmoten.odata.client.CollectionPageEntity;
import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.CollectionPageNonEntity;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.EntityPreconditions;
import com.github.davidmoten.odata.client.EntityRequest;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.NameValue;
import com.github.davidmoten.odata.client.ODataEntityType;
import com.github.davidmoten.odata.client.ODataType;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.StreamProvider;
import com.github.davidmoten.odata.client.TestingService.BuilderBase;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;
import com.github.davidmoten.odata.client.annotation.NavigationProperty;
import com.github.davidmoten.odata.client.annotation.Property;
import com.github.davidmoten.odata.client.generator.model.Action;
import com.github.davidmoten.odata.client.generator.model.Action.Parameter;
import com.github.davidmoten.odata.client.generator.model.Action.ReturnType;
import com.github.davidmoten.odata.client.generator.model.ComplexType;
import com.github.davidmoten.odata.client.generator.model.EntityType;
import com.github.davidmoten.odata.client.generator.model.Field;
import com.github.davidmoten.odata.client.generator.model.Function;
import com.github.davidmoten.odata.client.generator.model.KeyElement;
import com.github.davidmoten.odata.client.generator.model.Method;
import com.github.davidmoten.odata.client.generator.model.Structure;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.EdmSchemaInfo;
import com.github.davidmoten.odata.client.internal.ParameterMap;
import com.github.davidmoten.odata.client.internal.RequestHelper;
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

        for (Schema schema : schemas) {

            System.out.println("generating for namespace=" + schema.getNamespace());

            System.out.println("  replacing aliases");
            Util.replaceAliases(schema);

            System.out.println("  creating maps");
            Map<String, List<Action>> typeActions = createTypeActions(schema, names, false);
            System.out.println("    actions count = " + typeActions.size());

            Map<String, List<Function>> typeFunctions = createTypeFunctions(schema, names, false);
            System.out.println("    functions count = " + typeFunctions.size());
            
            Map<String, List<Action>> collectionTypeActions = createTypeActions(schema, names, true);
            System.out.println("    collection actions count = " + collectionTypeActions.size());

            Map<String, List<Function>> collectionTypeFunctions = createTypeFunctions(schema, names, true);
            System.out.println("    collection functions count = " + collectionTypeFunctions.size());

            System.out.println("  writing schema info");
            writeSchemaInfo(schema);

            // write enums
            System.out.println("  writing enums");
            Util.types(schema, TEnumType.class) //
                    .forEach(x -> writeEnum(schema, x));

            // write entityTypes
            System.out.println("  writing entities");
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> writeEntity(x, typeActions, typeFunctions));

            // write complexTypes
            System.out.println("  writing complex types");
            Util.types(schema, TComplexType.class) //
                    .forEach(x -> writeComplexType(schema, x));

            // write entity collection requests
            System.out.println("  writing entity collection requests");
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> writeEntityCollectionRequest(schema, x, collectionTypeActions, collectionTypeFunctions));

            // write containers
            System.out.println("  writing container");
            Util.types(schema, TEntityContainer.class) //
                    .forEach(x -> writeContainer(schema, x));

            // write single requests
            System.out.println("  writing entity requests");
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> writeEntityRequest(schema, x, typeActions, typeFunctions));

            System.out.println("  writing complex type requests");
            Util.types(schema, TComplexType.class) //
                    .forEach(x -> writeComplexTypeRequest(schema, x));

            // TODO write actions

            // TODO write functions

            // TODO consume annotations for documentation
        }

    }

    private Map<String, List<Action>> createTypeActions(Schema schema, Names names, boolean collectionsOnly) {
        return createMap(TAction.class, schema, names, action -> new Action(action, names), collectionsOnly);
    }

    private Map<String, List<Function>> createTypeFunctions(Schema schema, Names names, boolean collectionsOnly) {
        return createMap(TFunction.class, schema, names, function -> new Function(function, names), collectionsOnly);
    }

    @SuppressWarnings("unchecked")
    private <T, S extends Method> Map<String, List<S>> createMap(Class<T> cls, Schema schema,
            Names names, java.util.function.Function<T, S> mapper, boolean collectionsOnly) {
        Map<String, List<S>> map = new HashMap<>();
        Util.types(schema, cls) //
                .forEach(method -> {
                    S a = mapper.apply(method);
                    if ((!collectionsOnly && !a.isBoundToCollection()) || (collectionsOnly && a.isBoundToCollection())) {
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
                p.format("public enum %s implements %s {\n\n", simpleClassName,
                        imports.add(SchemaInfo.class));

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
                                sch -> Util.filter(sch.getComplexTypeOrEntityTypeOrTypeDefinition(),
                                        TEntityType.class)) //
                        .forEach(x -> {
                            Schema sch = names.getSchema(x);
                            p.format("%sclasses.put(\"%s\", %s.class);\n", indent,
                                    names.getFullTypeFromSimpleType(sch, x.getName()),
                                    imports.add(names.getFullClassNameEntity(sch, x.getName())));
                        });
                names //
                        .getSchemas() //
                        .stream() //
                        .flatMap(
                                sch -> Util.filter(sch.getComplexTypeOrEntityTypeOrTypeDefinition(),
                                        TComplexType.class)) //
                        .forEach(x -> {
                            Schema sch = names.getSchema(x);
                            p.format("%sclasses.put(\"%s\", %s.class);\n", indent,
                                    names.getFullTypeFromSimpleType(sch, x.getName()), imports.add(
                                            names.getFullClassNameComplexType(sch, x.getName())));
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
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileSchema(schema).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                p.format("%sprivate %s(%s name, %s value) {\n", indent, simpleClassName,
                        imports.add(String.class), imports.add(String.class));
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
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
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

            printJsonIncludeNonNull(imports, p);
            printPropertyOrder(imports, p, t.getProperties());
            p.format("public class %s%s implements %s {\n", simpleClassName,
                    t.getExtendsClause(imports), imports.add(ODataEntityType.class));

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

            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic %s getChangedFields() {\n", indent,
                    imports.add(ChangedFields.class));
            p.format("%sreturn changedFields;\n", indent.right());
            p.format("%s}\n", indent.left());

            KeyInfo k = getKeyInfo(t, imports);
            String nullCheck = k //
                    .fieldNames //
                            .stream() //
                            .map(f -> f + " != null") //
                            .collect(Collectors.joining(" && "));
            if (!nullCheck.isEmpty()) {
                nullCheck = " && " + nullCheck;
            }
            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic void postInject(boolean addKeysToContextPath) {\n", indent);
            p.format("%sif (addKeysToContextPath%s) {\n", indent.right(), nullCheck);
            p.format("%scontextPath = contextPath.clearQueries()%s;\n", indent.right(), k.addKeys);
            p.format("%s}\n", indent.left());
            p.format("%s}\n", indent.left());

            // write property getter and setters
            printPropertyGetterAndSetters(imports, indent, p, simpleClassName, t.getFullType(),
                    t.getProperties(), t.getFields(imports), true);
            printNavigationPropertyGetters(imports, indent, p, t.getNavigationProperties());

            addUnmappedFieldsSetterAndGetter(imports, indent, p);

            if (t.hasStream()) {
                p.format("\n%s/**\n", indent);
                p.format(
                        "%s * If suitable metadata found a StreamProvider is returned otherwise returns\n",
                        indent);
                p.format(
                        "%s * {@code Optional.empty()}. Normally for a stream to be available this entity\n",
                        indent);
                p.format(
                        "%s * needs to have been hydrated with full metadata. Consider calling the builder\n",
                        indent);
                p.format(
                        "%s * method {@code .metadataFull()} when getting this instance (either directly or\n",
                        indent);
                p.format("%s * as part of a collection).\n", indent);
                p.format("%s *\n", indent);
                p.format(
                        "%s * @return StreamProvider if suitable metadata found otherwise returns\n",
                        indent);
                p.format("%s *         {@code Optional.empty()}\n", indent);
                p.format("%s */\n", indent);
                p.format("%spublic %s<%s> getStream() {\n", indent, imports.add(Optional.class),
                        imports.add(StreamProvider.class));
                p.format("%sreturn %s.createStream(contextPath, this);\n", indent.right(),
                        imports.add(RequestHelper.class));
                p.format("%s}\n", indent.left());
            }

            // write Patched class
            writePatchAndPutMethods(t, simpleClassName, imports, indent, p);

            writeCopyMethod(t, simpleClassName, imports, indent, p, true);

            writeBoundActionMethods(t, typeActions, imports, indent, p);

            writeBoundFunctionMethods(t, typeFunctions, imports, indent, p);

            // write toString
            writeToString(t, simpleClassName, imports, indent, p);

            p.format("%s}\n", indent.left());

            writeToFile(imports, w, t.getClassFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeBoundActionMethods(EntityType t, Map<String, List<Action>> typeActions,
            Imports imports, Indent indent, PrintWriter p) {
        typeActions //
                .getOrDefault(t.getFullType(), Collections.emptyList()) //
                .forEach(action -> {
                    p.format("\n%s@%s(name = \"%s\")\n", //
                            indent, //
                            imports.add(com.github.davidmoten.odata.client.annotation.Action.class), //
                            action.getName());
                    List<Parameter> parameters = action.getParametersUnbound(imports);
                    String paramsDeclaration = parameters //
                            .stream() //
                            .map(x -> String.format("%s %s", x.importedFullClassName, x.nameJava)) //
                            .collect(Collectors.joining(", "));
                    if (action.hasReturnType()) {
                        ReturnType returnType = action.getReturnType(imports);
                        p.format("%spublic %s<%s> %s(%s) {\n", //
                                indent, //
                                returnType.isCollection
                                        ? imports.add(ActionFunctionRequestReturningCollection.class)
                                        : imports.add(ActionFunctionRequestReturningNonCollection.class), //
                                action.getReturnType(imports).innerImportedFullClassName,
                                action.getActionMethodName(), paramsDeclaration);
                        writeActionParameterMap(imports, indent, p, parameters);
                        if (returnType.isCollection) {
                            p.format(
                                    "%sreturn new %s<%s>(this.contextPath.addSegment(\"%s\"), %s.class, _parameters);\n", //
                                    indent, //
                                    imports.add(ActionFunctionRequestReturningCollection.class), //
                                    returnType.innerImportedFullClassName, //
                                    action.getFullType(), //
                                    returnType.innerImportedFullClassName);
                        } else {
                            p.format(
                                    "%sreturn new %s<%s>(this.contextPath.addSegment(\"%s\"), %s.class, _parameters, %s.INSTANCE);\n", //
                                    indent, //
                                    imports.add(ActionFunctionRequestReturningNonCollection.class), //
                                    returnType.innerImportedFullClassName, //
                                    action.getFullType(), //
                                    returnType.innerImportedFullClassName,
                                    imports.add(action.getReturnTypeFullClassNameSchemaInfo()));
                        }
                    } else {
                        p.format("%spublic %s %s(%s) {\n", //
                                indent, //
                                imports.add(ActionRequestNoReturn.class), //
                                action.getActionMethodName(), paramsDeclaration);
                        writeActionParameterMap(imports, indent, p, parameters);
                        p.format(
                                "%sreturn new %s(this.contextPath.addSegment(\"%s\"), _parameters);\n", //
                                indent, //
                                imports.add(ActionRequestNoReturn.class), //
                                action.getFullType());
                    }
                    p.format("%s}\n", indent.left());
                });
    }

    private void writeBoundFunctionMethods(EntityType t, Map<String, List<Function>> typeFunctions,
            Imports imports, Indent indent, PrintWriter p) {
        typeFunctions //
                .getOrDefault(t.getFullType(), Collections.emptyList()) //
                .forEach(function -> {
                    p.format("\n%s@%s(name = \"%s\")\n", //
                            indent, //
                            imports.add(
                                    com.github.davidmoten.odata.client.annotation.Function.class), //
                            function.getName());
                    List<Function.Parameter> parameters = function.getParametersUnbound(imports);
                    String paramsDeclaration = parameters //
                            .stream() //
                            .map(x -> String.format("%s %s", x.importedFullClassName, x.nameJava)) //
                            .collect(Collectors.joining(", "));
                    Function.ReturnType returnType = function.getReturnType(imports);
                    p.format("%spublic %s<%s> %s(%s) {\n", //
                            indent, //
                            returnType.isCollection
                                    ? imports.add(ActionFunctionRequestReturningCollection.class)
                                    : imports.add(ActionFunctionRequestReturningNonCollection.class), //
                            function.getReturnType(imports).innerImportedFullClassName,
                            function.getActionMethodName(), paramsDeclaration);
                    writeFunctionParameterMap(imports, indent, p, parameters);
                    if (returnType.isCollection) {
                        p.format(
                                "%sreturn new %s<%s>(this.contextPath.addSegment(\"%s\"), %s.class, _parameters);\n", //
                                indent, //
                                imports.add(ActionFunctionRequestReturningCollection.class), //
                                returnType.innerImportedFullClassName, //
                                function.getFullType(), //
                                returnType.innerImportedFullClassName);
                    } else {
                        p.format(
                                "%sreturn new %s<%s>(this.contextPath.addSegment(\"%s\"), %s.class, _parameters, %s.INSTANCE);\n", //
                                indent, //
                                imports.add(ActionFunctionRequestReturningNonCollection.class), //
                                returnType.innerImportedFullClassName, //
                                function.getFullType(), //
                                returnType.innerImportedFullClassName,
                                imports.add(function.getReturnTypeFullClassNameSchemaInfo()));
                    }
                    p.format("%s}\n", indent.left());
                });
    }

    private void writeActionParameterMap(Imports imports, Indent indent, PrintWriter p,
            List<Parameter> parameters) {
        AtomicBoolean first = new AtomicBoolean(true);
        p.format("%s%s<%s, %s> _parameters = %s%s;\n", //
                indent.right(), //
                imports.add(Map.class), //
                imports.add(String.class), //
                imports.add(Object.class), //
                imports.add(ParameterMap.class), //
                parameters.isEmpty() ? String.format(".empty()")
                        : parameters //
                                .stream() //
                                .map(par -> String.format("\n%s.put(\"%s\", %s)", //
                                        indent.copy().right(),
                                        par.name, //
                                        par.nameJava)) //
                                .collect(Collectors.joining()) + "\n" + indent.copy().right() + ".build()");
    }

    private void writeFunctionParameterMap(Imports imports, Indent indent, PrintWriter p,
            List<Function.Parameter> parameters) {
        AtomicBoolean first = new AtomicBoolean(true);
        p.format("%s%s<%s, %s> _parameters = %s%s;\n", //
                indent.right(), //
                imports.add(Map.class), //
                imports.add(String.class), //
                imports.add(Object.class), //
                imports.add(ParameterMap.class), //
                parameters.isEmpty() ? String.format(".empty()")
                        : parameters //
                                .stream() //
                                .map(par -> String.format("\n%s.put(\"%s\", %s)", //
                                        indent.copy().right(),
                                        par.name, //
                                        par.nameJava)) //
                                .collect(Collectors.joining()) + "\n" + indent.copy().right() + ".build()");
    }

    private void writeToString(Structure<?> t, String simpleClassName, Imports imports,
            Indent indent, PrintWriter p) {
        p.format("\n%s@%s\n", indent, imports.add(Override.class));
        p.format("%spublic %s toString() {\n", indent, imports.add(String.class));
        p.format("%s%s b = new %s();\n", indent.right(), imports.add(StringBuilder.class),
                imports.add(StringBuilder.class));
        p.format("%sb.append(\"%s[\");\n", indent, simpleClassName);
        boolean[] first = new boolean[1];
        first[0] = true;
        t.getFields(imports).stream().forEach(f -> {
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

    private void writeCopyMethod(Structure<?> t, String simpleClassName, Imports imports,
            Indent indent, PrintWriter p, boolean ofEntity) {
        List<Field> fields = t.getFields(imports);
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

    private void writeNoArgsConstructor(String simpleClassName, Indent indent, PrintWriter p,
            boolean hasBaseType) {
        p.format("\n%sprotected %s() {\n", indent, simpleClassName);
        indent.right();
        if (hasBaseType) {
            p.format("%ssuper();\n", indent);
        }
        p.format("%s}\n", indent.left());
    }

    private void printJsonIncludeNonNull(Imports imports, PrintWriter p) {
        p.format("@%s(%s.NON_NULL)\n", imports.add(JsonInclude.class), imports.add(Include.class));
    }

    private void writePatchAndPutMethods(EntityType t, String simpleClassName, Imports imports,
            Indent indent, PrintWriter p) {
        // write patch() method
        writePutOrPatchMethod(t, simpleClassName, imports, indent, p, true);

        // write put method
        writePutOrPatchMethod(t, simpleClassName, imports, indent, p, false);

    }

    private void writePutOrPatchMethod(EntityType t, String simpleClassName, Imports imports,
            Indent indent, PrintWriter p, boolean isPatch) {
        String methodName = isPatch ? "patch" : "put";
        p.format("\n%spublic %s %s() {\n", indent, simpleClassName, methodName);
        p.format("%s%s.%s(this, contextPath, %s.EMPTY);\n", indent.right(),
                imports.add(RequestHelper.class), methodName, imports.add(RequestOptions.class));

        // use _x as identifier so doesn't conflict with any field name
        p.format("%s%s _x = _copy();\n", indent, simpleClassName);
        p.format("%s_x.changedFields = null;\n", indent);
        p.format("%sreturn _x;\n", indent);
        p.format("%s}\n", indent.left());
    }

    private static void addChangedFieldsField(Imports imports, Indent indent, PrintWriter p) {
        p.format("\n%s@%s", indent, imports.add(JacksonInject.class));
        p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
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

            printJsonIncludeNonNull(imports, p);
            printPropertyOrder(imports, p, t.getProperties());
            p.format("public class %s%s implements %s {\n\n", simpleClassName,
                    t.getExtendsClause(imports), imports.add(ODataType.class));

            indent.right();
            if (!t.hasBaseType()) {
                addContextPathField(imports, indent, p);
            }

            addUnmappedFieldsField(imports, indent, p);

            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic String odataTypeName() {\n", indent);
            p.format("%sreturn \"%s\";\n", indent.right(), t.getFullType());
            p.format("%s}\n", indent.left());

            // write fields from properties
            printPropertyFields(imports, indent, p, t.getProperties(), t.hasBaseType());

            // write constructor
            writeNoArgsConstructor(simpleClassName, indent, p, t.hasBaseType());

            printPropertyGetterAndSetters(imports, indent, p, simpleClassName, t.getFullType(),
                    t.getProperties(), t.getFields(imports), false);

            addUnmappedFieldsSetterAndGetter(imports, indent, p);

            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic void postInject(boolean addKeysToContextPath) {\n", indent);
            p.format("%s// do nothing;\n", indent.right());
            p.format("%s}\n", indent.left());

            writeBuilder(t, simpleClassName, imports, indent, p);

            // write copy method
            writeCopyMethod(t, simpleClassName, imports, indent, p, false);

            // write toString
            writeToString(t, simpleClassName, imports, indent, p);

            p.format("\n}\n");
            writeToFile(imports, w, t.getClassFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEntityRequest(Schema schema, TEntityType entityType,
            Map<String, List<Action>> typeActions, Map<String, List<Function>> typeFunctions) {
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
            p.format("public final class %s extends %s {\n\n", simpleClassName,
                    imports.add(EntityRequest.class) + "<" + imports.add(t.getFullClassNameEntity())
                            + ">");

            indent.right();

            // add constructor
            p.format("%spublic %s(%s contextPath) {\n", indent, simpleClassName,
                    imports.add(ContextPath.class), imports.add(String.class));
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
                        boolean isEntity = names
                                .isEntityWithNamespace(names.getInnerType(names.getType(x)));
                        if (!isEntity) {
                            System.out.println(
                                    "Unexpected entity with non-entity navigation property type: "
                                            + simpleClassName + "." + x.getName()
                                            + ". If you get this message then raise an issue on the github project for odata-client.");
                        }
                        return isEntity;
                    }) //
                    .forEach(x -> {
                        indent.right();
                        final String returnClass;
                        String y = x.getType().get(0);
                        Schema sch = names.getSchema(names.getInnerType(y));
                        if (y.startsWith(COLLECTION_PREFIX)) {
                            String inner = names.getInnerType(y);
                            returnClass = imports.add(CollectionPageEntityRequest.class) + "<"
                                    + imports
                                            .add(names.getFullClassNameFromTypeWithNamespace(inner))
                                    + ", "
                                    + imports.add(names
                                            .getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                                    inner))
                                    + ">";
                        } else {
                            returnClass = imports.add(names
                                    .getFullClassNameEntityRequestFromTypeWithNamespace(sch, y));
                        }
                        p.format("\n%spublic %s %s() {\n", //
                                indent, //
                                returnClass, //
                                Names.getGetterMethodWithoutGet(x.getName()));
                        if (isCollection(x)) {
                            p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                            p.format("%scontextPath.addSegment(\"%s\"),\n",
                                    indent.right().right().right().right(), x.getName());
                            p.format("%s%s.class,\n", indent,
                                    imports.add(names.getFullClassNameFromTypeWithNamespace(
                                            names.getInnerType(names.getType(x)))));
                            p.format("%scontextPath -> new %s(contextPath), %s.INSTANCE);\n",
                                    indent,
                                    imports.add(names
                                            .getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                                    names.getInnerType(names.getType(x)))),
                                    imports.add(names.getFullClassNameSchemaInfo(sch)));
                            indent.left().left().left().left();
                        } else {
                            p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n",
                                    indent.right(), returnClass, x.getName());
                        }
                        p.format("%s}\n", indent.left());

                        // if collection then add with id method
                        if (y.startsWith(COLLECTION_PREFIX)) {
                            // TODO use actual key name from metadata
                            String inner = names.getInnerType(y);
                            // TODO remove redundant check
                            if (names.isEntityWithNamespace(inner)) {
                                String entityRequestType = names
                                        .getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                                inner);
                                EntityType et = names.getEntityType(inner);
                                KeyInfo k = getKeyInfo(et, imports);

                                p.format("\n%spublic %s %s(%s) {\n", indent,
                                        imports.add(entityRequestType),
                                        Names.getIdentifier(x.getName()), k.typedParams);
                                p.format("%sreturn new %s(contextPath.addSegment(\"%s\")%s);\n",
                                        indent.right(), imports.add(entityRequestType), x.getName(),
                                        k.addKeys);
                                p.format("%s}\n", indent.left());
                            }
                        }
                        indent.left();
                    });
            writeBoundActionMethods(t, typeActions, imports, indent, p);
            writeBoundFunctionMethods(t, typeFunctions, imports, indent, p);
            p.format("\n}\n");
            writeToFile(imports, w, t.getClassFileEntityRequest());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class KeyInfo {
        final List<String> fieldNames;
        final String typedParams;
        final String addKeys;

        KeyInfo(List<String> fields, String typedParams, String addKeys) {
            this.fieldNames = fields;
            this.typedParams = typedParams;
            this.addKeys = addKeys;
        }
    }

    private KeyInfo getKeyInfo(EntityType et, Imports imports) {
        KeyElement key = et.getFirstKey();

        List<String> fields = key //
                .getPropertyRefs() //
                .stream() //
                .map(z -> z.getReferredProperty().getFieldName()) //
                .collect(Collectors.toList());

        String typedParams = key //
                .getPropertyRefs() //
                .stream() //
                .map(z -> z.getReferredProperty()) //
                .map(z -> String.format("%s %s", z.getImportedType(imports), z.getFieldName())) //
                .collect(Collectors.joining(", "));

        String addKeys = et.getFirstKey() //
                .getPropertyRefs() //
                .stream() //
                .map(z -> z.getReferredProperty()) //
                .map(z -> {
                    if (key.getPropertyRefs().size() > 1) {
                        return String.format("new %s(\"%s\", %s)", imports.add(NameValue.class),
                                z.getName(), z.getFieldName());
                    } else {
                        return String.format("new %s(%s.toString())", imports.add(NameValue.class),
                                z.getFieldName());
                    }
                }) //
                .collect(Collectors.joining(", "));
        addKeys = ".addKeys(" + addKeys + ")";
        return new KeyInfo(fields, typedParams, addKeys);
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
                extension = " extends "
                        + imports.add(names.getFullClassNameFromTypeWithNamespace(t.getExtends()));
            } else {
                extension = "";
            }
            p.format("public final class %s%s {\n\n", simpleClassName, extension);

            // TODO handle container extension

            // write fields
            p.format("%sprivate final %s contextPath;\n\n", indent.right(),
                    imports.add(ContextPath.class));

            // write constructor
            p.format("%spublic %s(%s context) {\n", indent, simpleClassName,
                    imports.add(Context.class));
            p.format("%sthis.contextPath = new %s(context, context.service().getBasePath());\n",
                    indent.right(), imports.add(ContextPath.class));
            p.format("%s}\n", indent.left());

            p.format("\n%spublic %s _context() {\n", indent, imports.add(Context.class));
            p.format("%sreturn contextPath.context();\n", indent.right());
            p.format("%s}\n", indent.left());

            p.format("\n%spublic %s _service() {\n", indent, imports.add(HttpService.class));
            p.format("%sreturn contextPath.context().service();\n", indent.right());
            p.format("%s}\n", indent.left());

            // write static testing method
            p.format("\n%sstatic final class ContainerBuilderImpl extends %s<%s> {\n", indent,
                    imports.add(ContainerBuilder.class), simpleClassName);
            p.format("\n%s@%s\n", indent.right(), imports.add(Override.class));
            p.format("%spublic %s _create(%s context) {\n", indent, simpleClassName,
                    imports.add(Context.class));
            p.format("%sreturn new %s(context);\n", indent.right(), simpleClassName);
            p.format("%s}\n", indent.left());
            p.format("%s}\n", indent.left());

            p.format("\n%spublic static %s<%s<%s>, %s> test() {\n", indent,
                    imports.add(BuilderBase.class), imports.add(ContainerBuilder.class),
                    simpleClassName, simpleClassName);
            p.format("%sreturn new ContainerBuilderImpl();\n", indent.right());
            p.format("%s}\n", indent.left());

            // write get methods from properties
            Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
                    .forEach(x -> {
                        Schema sch = names.getSchema(x.getEntityType());
                        p.format("\n%spublic %s %s() {\n", indent, toType(x, imports),
                                Names.getIdentifier(x.getName()));
                        p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                        p.format("%scontextPath.addSegment(\"%s\"));\n",
                                indent.right().right().right().right(), x.getName());
//                        p.format("%s%s.class,\n", indent, imports.add(
//                                names.getFullClassNameFromTypeWithNamespace(x.getEntityType())));
//                        p.format("%scontextPath -> new %s(contextPath), %s.INSTANCE);\n", indent,
//                                imports.add(
//                                        names.getFullClassNameEntityRequestFromTypeWithNamespace(
//                                                sch, x.getEntityType())), //
//                                imports.add(names.getFullClassNameSchemaInfo(sch)));
                        p.format("%s}\n", indent.left().left().left().left().left());

                        if (names.isEntityWithNamespace(x.getEntityType())) {
                            String entityRequestType = names
                                    .getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                            x.getEntityType());

                            EntityType et = names.getEntityType(x.getEntityType());
                            KeyInfo k = getKeyInfo(et, imports);

                            p.format("\n%spublic %s %s(%s) {\n", indent,
                                    imports.add(entityRequestType),
                                    Names.getIdentifier(x.getName()), k.typedParams);
                            p.format("%sreturn new %s(contextPath.addSegment(\"%s\")%s);\n",
                                    indent.right(), imports.add(entityRequestType), x.getName(),
                                    k.addKeys);
                            p.format("%s}\n", indent.left());
                        }
                    });

            Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TSingleton.class) //
                    .forEach(x -> {
                        String importedType = toType(x, imports);
                        p.format("\n%spublic %s %s() {\n", indent, importedType,
                                Names.getIdentifier(x.getName()));
                        p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n",
                                indent.right(), importedType, x.getName());
                        p.format("%s}\n", indent.left());
                    });

            p.format("\n}\n");
            File classFile = names.getClassFileContainer(schema, t.getName());
            writeToFile(imports, w, classFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEntityCollectionRequest(Schema schema, TEntityType entityType, Map<String, List<Action>> collectionTypeActions, Map<String, List<Function>> collectionTypeFunctions) {
        EntityType t = new EntityType(entityType, names);
        names.getDirectoryEntityCollectionRequest(schema).mkdirs();
        String simpleClassName = names.getSimpleClassNameCollectionRequest(schema, t.getName());
        Imports imports = new Imports(names.getFullClassNameCollectionRequest(schema, t.getName()));
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageCollectionRequest(schema));
            p.format("IMPORTSHERE");
            p.format("public final class %s extends %s<%s, %s>{\n\n", simpleClassName,
                    imports.add(CollectionPageEntityRequest.class),
                    imports.add(
                            names.getFullClassNameFromTypeWithoutNamespace(schema, t.getName())), //
                    imports.add(names.getFullClassNameEntityRequest(schema, t.getName())));

            indent.right();
            addContextPathField(imports, indent, p);

            // add constructor
            p.format("\n%spublic %s(%s contextPath) {\n", indent, simpleClassName,
                    imports.add(ContextPath.class), imports.add(String.class));
            p.format("%ssuper(contextPath, %s.class, cp -> new %s(cp), %s.INSTANCE);\n",
                    indent.right(),
                    imports.add(
                            names.getFullClassNameFromTypeWithoutNamespace(schema, t.getName())), //
                    imports.add(names.getFullClassNameEntityRequestFromTypeWithoutNamespace(schema,
                            t.getName())), //
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
                                    imports.add(names
                                            .getFullClassNameCollectionRequestFromTypeWithNamespace(
                                                    sch, y)), //
                                    Names.getIdentifier(x.getName()));

                            p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", //
                                    indent.right(), //
                                    imports.add(names
                                            .getFullClassNameCollectionRequestFromTypeWithNamespace(
                                                    sch, y)), //
                                    x.getName());
                            p.format("%s}\n", indent.left());

                            if (names.isEntityWithNamespace(y)) {
                                String entityRequestType = names
                                        .getFullClassNameEntityRequestFromTypeWithNamespace(sch, y);
                                EntityType et = names.getEntityType(y);
                                KeyInfo k = getKeyInfo(et, imports);

                                p.format("\n%spublic %s %s(%s) {\n", indent,
                                        imports.add(entityRequestType),
                                        Names.getIdentifier(x.getName()), k.typedParams);
                                p.format("%sreturn new %s(contextPath.addSegment(\"%s\")%s);\n",
                                        indent.right(), imports.add(entityRequestType), x.getName(),
                                        k.addKeys);
                                p.format("%s}\n", indent.left());
                            }
                        }
                    });
            
            writeBoundActionMethods(t, collectionTypeActions, imports, indent, p);
            writeBoundFunctionMethods(t, collectionTypeFunctions, imports, indent, p);
            
            indent.left();
            p.format("\n}\n");
            writeToFile(imports, w, t.getClassFileCollectionRequest());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void writeBuilder(Structure<?> t, String simpleClassName, Imports imports,
            Indent indent, PrintWriter p) {
        if (t.isAbstract()) {
            return;
        }
        String builderSuffix = t.getBaseType() == null ? "" : simpleClassName;

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
            p.format("%sprivate %s changedFields = new %s();\n", indent,
                    imports.add(ChangedFields.class), imports.add(ChangedFields.class));
        }

        p.format("\n%sBuilder() {\n", indent);
        p.format("%s// prevent instantiation\n", indent.right());
        p.format("%s}\n", indent.left());

        // write builder setters

        fields.forEach(f -> {
            p.format("\n%spublic Builder %s(%s %s) {\n", indent, f.fieldName, f.importedType,
                    f.fieldName);
            p.format("%sthis.%s = %s;\n", indent.right(), f.fieldName, f.fieldName);
            p.format("%sthis.changedFields = changedFields.add(\"%s\");\n", indent, f.name);
            p.format("%sreturn this;\n", indent);
            p.format("%s}\n", indent.left());
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
        fields.stream().map(f -> String.format("%s_x.%s = %s;\n", indent, f.fieldName, f.fieldName))
                .forEach(p::print);
        p.format("%sreturn _x;\n", indent);
        p.format("%s}\n", indent.left());

        p.format("%s}\n", indent.left());
    }

    private static void addUnmappedFieldsField(Imports imports, Indent indent, PrintWriter p) {
        p.format("\n%s@%s", indent, imports.add(JacksonInject.class));
        p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
        p.format("%sprotected %s unmappedFields;\n", indent, imports.add(UnmappedFields.class));
    }

    private static void addUnmappedFieldsSetterAndGetter(Imports imports, Indent indent,
            PrintWriter p) {
        p.format("\n%s@%s\n", indent, imports.add(JsonAnySetter.class));
        // TODO protect "setUnmappedField" name against clashes
        p.format("%sprivate void setUnmappedField(String name, Object value) {\n", indent);
        p.format("%sif (unmappedFields == null) {\n", indent.right());
        p.format("%sunmappedFields = new %s();\n", indent.right(),
                imports.add(UnmappedFields.class));
        p.format("%s}\n", indent.left());
        p.format("%sunmappedFields.put(name, value);\n", indent);
        p.format("%s}\n", indent.left());

        p.format("\n%s@%s\n", indent, imports.add(Override.class));
        p.format("%spublic %s getUnmappedFields() {\n", indent, imports.add(UnmappedFields.class));
        p.format("%sreturn unmappedFields == null ? new %s() : unmappedFields;\n", indent.right(),
                imports.add(UnmappedFields.class));
        p.format("%s}\n", indent.left());
    }

    private static void addContextPathInjectableField(Imports imports, Indent indent,
            PrintWriter p) {
        // add context path field
        p.format("\n%s@%s", indent, imports.add(JacksonInject.class));
        p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
        addContextPathField(imports, indent, p);
    }

    private static void addContextPathField(Imports imports, Indent indent, PrintWriter p) {
        p.format("%sprotected %s%s contextPath;\n", indent, "", imports.add(ContextPath.class));
    }

    private void printPropertyGetterAndSetters(Imports imports, Indent indent, PrintWriter p,
            String simpleClassName, String fullType, List<TProperty> properties, List<Field> fields,
            boolean ofEntity) {

        // write getters and setters
        properties //
                .forEach(x -> {
                    String fieldName = Names.getIdentifier(x.getName());
                    String t = names.getType(x);
                    boolean isCollection = isCollection(x);
                    addPropertyAnnotation(imports, indent, p, x.getName());
                    if (isCollection) {
                        String inner = names.getInnerType(t);
                        String importedInnerType = names.toImportedTypeNonCollection(inner,
                                imports);
                        boolean isEntity = names.isEntityWithNamespace(inner);
                        Class<?> collectionCls;
                        if (isEntity) {
                            collectionCls = CollectionPageEntity.class;
                        } else {
                            collectionCls = CollectionPageNonEntity.class;
                        }
                        p.format("\n%spublic %s<%s> %s() {\n", indent, imports.add(collectionCls),
                                importedInnerType, Names.getGetterMethod(x.getName()));
                        if (isEntity) {
                            Schema sch = names.getSchema(inner);
                            p.format(
                                    "%sreturn %s.from(contextPath.context(), %s, %s.class, %s.INSTANCE);\n",
                                    indent.right(), imports.add(CollectionPageEntity.class),
                                    fieldName, importedInnerType,
                                    imports.add(names.getFullClassNameSchemaInfo(sch)));
                        } else {
                            final String importedSchemaInfo;
                            if (inner.startsWith("Edm.")) {
                                importedSchemaInfo = imports.add(EdmSchemaInfo.class);
                            } else {
                                Schema sch = names.getSchema(inner);
                                importedSchemaInfo = imports
                                        .add(names.getFullClassNameSchemaInfo(sch));
                            }
                            p.format(
                                    "%sreturn new %s<%s>(contextPath, %s.class, %s, %s.ofNullable(%sNextLink), %s.INSTANCE);\n",
                                    indent.right(), imports.add(CollectionPageNonEntity.class),
                                    importedInnerType, importedInnerType, fieldName,
                                    imports.add(Optional.class), fieldName, importedSchemaInfo);
                        }
                        p.format("%s}\n", indent.left());
                    } else {
                        boolean isStream = "Edm.Stream".equals(names.getType(x));
                        if (isStream) {
                            p.format("\n%spublic %s<%s> %s() {\n", indent,
                                    imports.add(Optional.class), imports.add(StreamProvider.class),
                                    Names.getGetterMethod(x.getName()));
                            p.format(
                                    "%sreturn %s.createStreamForEdmStream(contextPath, this, \"%s\", %s);\n",
                                    indent.right(), imports.add(RequestHelper.class), x.getName(),
                                    fieldName);
                            p.format("%s}\n", indent.left());
                            // TODO how to patch streamed content?
                        } else {
                            final String importedType = names.toImportedTypeNonCollection(t,
                                    imports);
                            String importedTypeWithOptional = imports.add(Optional.class) + "<"
                                    + importedType + ">";
                            p.format("\n%spublic %s %s() {\n", indent, importedTypeWithOptional,
                                    Names.getGetterMethod(x.getName()));
                            p.format("%sreturn %s.ofNullable(%s);\n", indent.right(),
                                    imports.add(Optional.class), fieldName);
                            p.format("%s}\n", indent.left());

                            String classSuffix = "";
                            p.format("\n%spublic %s%s %s(%s %s) {\n", indent, simpleClassName,
                                    classSuffix, Names.getWithMethod(x.getName()), importedType,
                                    fieldName);
                            if (x.isUnicode() != null && !x.isUnicode()) {
                                p.format("%s%s.checkIsAscii(%s);\n", indent.right(),
                                        imports.add(EntityPreconditions.class), fieldName,
                                        fieldName);
                                indent.left();
                            }
                            // use _x as identifier so doesn't conflict with any field name
                            p.format("%s%s _x = _copy();\n", indent.right(), simpleClassName,
                                    simpleClassName);
                            if (ofEntity) {
                                p.format("%s_x.changedFields = changedFields.add(\"%s\");\n",
                                        indent, x.getName());
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

    private void addPropertyAnnotation(Imports imports, Indent indent, PrintWriter p, String name) {
        p.format("\n%s@%s(name=\"%s\")", indent, imports.add(Property.class), name);
    }

    private void addNavigationPropertyAnnotation(Imports imports, Indent indent, PrintWriter p,
            String name) {
        p.format("\n%s@%s(name=\"%s\")", indent, imports.add(NavigationProperty.class), name);
    }

    private void printPropertyOrder(Imports imports, PrintWriter p, List<TProperty> properties) {
        String props = Stream.concat( //
                Stream.of("@odata.type"), properties.stream().map(x -> x.getName())) //
                .map(x -> "\n    \"" + x + "\"") //
                .collect(Collectors.joining(", "));
        p.format("@%s({%s})\n", imports.add(JsonPropertyOrder.class), props);
    }

    private void printPropertyFields(Imports imports, Indent indent, PrintWriter p,
            List<TProperty> properties, boolean hasBaseType) {

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
                p.format("\n%s@%s(\"%s@nextLink\")\n", indent, imports.add(JsonProperty.class),
                        x.getName());
                p.format("%sprotected %s %sNextLink;\n", indent, imports.add(String.class),
                        Names.getIdentifier(x.getName()));
            }
        });
    }

    private void printNavigationPropertyGetters(Imports imports, Indent indent, PrintWriter p,
            List<TNavigationProperty> properties) {
        // write getters
        properties //
                .stream() //
                .forEach(x -> {
                    String typeName = toType(x, imports);
                    addNavigationPropertyAnnotation(imports, indent, p, x.getName());
                    p.format("\n%spublic %s %s() {\n", indent, typeName,
                            Names.getGetterMethod(x.getName()));
                    if (isCollection(x)) {
                        if (names.isEntityWithNamespace(names.getType(x))) {
                            Schema sch = names.getSchema(names.getInnerType(names.getType(x)));
                            p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                            p.format("%scontextPath.addSegment(\"%s\"),\n", //
                                    indent.right().right().right().right(), x.getName());
                            p.format("%s%s.class,\n", indent,
                                    imports.add(names.getFullClassNameFromTypeWithNamespace(
                                            names.getInnerType(names.getType(x)))));
                            p.format("%scontextPath -> new %s(contextPath), %s.INSTANCE);\n",
                                    indent,
                                    imports.add(names
                                            .getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                                    names.getInnerType(names.getType(x)))), //
                                    imports.add(names.getFullClassNameSchemaInfo(sch)));
                            indent.left().left().left().left();
                        } else {
                            throw new RuntimeException("unexpected");
                        }
                    } else {
                        if (names.isEntityWithNamespace(names.getType(x))) {
                            Schema sch = names.getSchema(names.getInnerType(names.getType(x)));
                            p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", //
                                    indent.right(), //
                                    imports.add(names
                                            .getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                                    names.getInnerType(names.getType(x)))),
                                    x.getName());
                        } else {
                            throw new RuntimeException("unexpected");
                        }
                    }
                    p.format("%s}\n", indent.left());
                });
    }

    private String toType(TNavigationProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        if (!isCollection(x)) {
            if (x.isNullable() != null && x.isNullable()) {
                String r = names.toImportedFullClassName(t, imports, List.class);
                return imports.add(Optional.class) + "<" + r + ">";
            } else {
                // is navigation property so must be an entity and is a single request
                Schema sch = names.getSchema(names.getInnerType(t));
                return imports
                        .add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, t));
            }
        } else {
            return names.toImportedFullClassName(t, imports, CollectionPageEntityRequest.class);
        }
    }

    private String toType(TSingleton x, Imports imports) {
        String t = x.getType();
        if (!isCollection(x.getType())) {
            Schema sch = names.getSchema(names.getInnerType(t));
            return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, t));
        } else {
            return names.toImportedFullClassName(t, imports, CollectionPageEntityRequest.class);
        }
    }

    private String toType(TEntitySet x, Imports imports) {
        String t = x.getEntityType();
        // an entity set is always a collection
        Schema schema = names.getSchema(t);
        return imports.add(names.getFullClassNameCollectionRequestFromTypeWithNamespace(schema, t));
//        return names.wrapCollection(imports, CollectionPageEntityRequest.class, t);
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
