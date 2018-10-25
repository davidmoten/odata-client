package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityContainer;
import org.oasisopen.odata.csdl.v4.TEntitySet;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;
import org.oasisopen.odata.csdl.v4.TSingleton;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.CollectionPageEntity;
import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.CollectionPageNonEntity;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.EntityPreconditions;
import com.github.davidmoten.odata.client.EntityRequest;
import com.github.davidmoten.odata.client.HasUnmappedFields;
import com.github.davidmoten.odata.client.NameValue;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.Patchable;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.StreamProvider;
import com.github.davidmoten.odata.client.TestingService.BuilderBase;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;
import com.github.davidmoten.odata.client.generator.model.ComplexType;
import com.github.davidmoten.odata.client.generator.model.EntityType;
import com.github.davidmoten.odata.client.generator.model.Field;
import com.github.davidmoten.odata.client.generator.model.KeyElement;
import com.github.davidmoten.odata.client.generator.model.Structure;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.UnmappedFields;

public final class Generator {

    private static final String CLASS_NAME_PATCHED = "Patched";
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

            writeSchemaInfo(schema);

            // write enums
            Util.types(schema, TEnumType.class) //
                    .forEach(x -> writeEnum(schema, x));

            // write entityTypes
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> writeEntity(x));

            // write complexTypes
            Util.types(schema, TComplexType.class) //
                    .forEach(x -> writeComplexType(schema, x));

            // write collection requests
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> writeCollectionRequest(schema, x));

            // write containers
            Util.types(schema, TEntityContainer.class) //
                    .forEach(x -> writeContainer(schema, x));

            // write single requests
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> writeEntityRequest(schema, x));

            // TODO write actions

            // TODO write functions

            // TODO consume annotations for documentation
        }

    }

    private void writeSchemaInfo(Schema schema) {
        names.getDirectorySchema(schema).mkdirs();
        String simpleClassName = names.getSimpleClassNameSchema(schema);
        Imports imports = new Imports(simpleClassName);
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
                p.format("%sprivate final %s<%s,%s<? extends %s>> entities = new %s<>();\n\n", //
                        indent, //
                        imports.add(Map.class), //
                        imports.add(String.class), //
                        imports.add(Class.class), //
                        imports.add(ODataEntity.class), //
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
                            p.format("%sentities.put(\"%s\", %s.class);\n", indent,
                                    names.getFullTypeFromSimpleType(schema, x.getName()),
                                    imports.add(names.getFullClassNameEntity(sch, x.getName())));
                        });
                indent.left();

                p.format("%s}\n\n", indent);

                // add method
                p.format("%s@%s\n", indent, imports.add(Override.class));
                p.format("%spublic %s<? extends %s> getEntityClassFromTypeWithNamespace(%s name) {\n", //
                        indent, //
                        imports.add(Class.class), //
                        imports.add(ODataEntity.class), //
                        imports.add(String.class));
                p.format("%sreturn entities.get(name);\n", indent.right());
                p.format("%s}\n\n", indent.left());

                // close class
                p.format("}\n");
            }
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileSchema(schema).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void writeEnum(Schema schema, TEnumType t) {
        names.getDirectoryEnum(schema).mkdirs();
        String simpleClassName = names.getSimpleClassNameEnum(schema, t.getName());
        Imports imports = new Imports(simpleClassName);
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

    private void writeEntity(TEntityType entityType) {
        EntityType t = new EntityType(entityType, names);
        t.getDirectoryEntity().mkdirs();
        String simpleClassName = t.getSimpleClassName();
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", t.getPackage());
            p.format("IMPORTSHERE");

            printJsonIncludeNonNull(imports, p);
            printPropertyOrder(imports, p, t.getProperties());
            p.format("public class %s%s implements %s {\n", simpleClassName, t.getExtendsClause(imports),
                    imports.add(ODataEntity.class));

            indent.right();
            if (!t.hasBaseType()) {
                addContextPathInjectableField(imports, indent, p);
                addUnmappedFieldsField(imports, indent, p);
                addChangedFieldsField(imports, indent, p);
            }

            // add other fields
            printPropertyFields(imports, indent, p, t.getProperties(), t.hasBaseType());

            // write constructor
            // build constructor parameters
            writeConstructorSignature(t, simpleClassName, imports, indent, p);
            indent.right();
            if (t.getBaseType() != null) {
                String superFields = t.getSuperFields(imports) //
                        .stream() //
                        .map(f -> ", " + f.fieldName) //
                        .collect(Collectors.joining());
                p.format("%ssuper(contextPath, changedFields, unmappedFields, odataType%s);\n", indent, superFields);
            }
            if (!t.hasBaseType()) {
                p.format("%sthis.contextPath = contextPath;\n", indent);
                p.format("%sthis.changedFields = changedFields;\n", indent);
                p.format("%sthis.unmappedFields = unmappedFields;\n", indent);
                p.format("%sthis.odataType = odataType;\n", indent);
            }

            // print constructor field assignments
            t.getProperties() //
                    .forEach(x -> {
                        String fieldName = Names.getIdentifier(x.getName());
                        p.format("%sthis.%s = %s;\n", indent, fieldName, fieldName);
                        if (isCollection(x) && !names.isEntityWithNamespace(names.getType(x))) {
                            p.format("%sthis.%sNextLink = %sNextLink;\n", indent, fieldName, fieldName);
                        }
                    });

            // close constructor
            p.format("%s}\n", indent.left());

            writeBuilder(t, simpleClassName, imports, indent, p);

            p.format("\n%spublic %s getChangedFields() {\n", indent, imports.add(ChangedFields.class));
            p.format("%sreturn changedFields;\n", indent.right());
            p.format("%s}\n", indent.left());

            // write property getter and setters
            printPropertyGetterAndSetters(imports, indent, p, simpleClassName, t.getFullType(), t.getProperties(),
                    t.getFields(imports), true);
            printNavigationPropertyGetters(imports, indent, p, t.getNavigationProperties());

            addUnmappedFieldsSetterAndGetter(imports, indent, p);

            if (t.hasStream()) {
                p.format("\n%spublic %s<%s> getStream() {\n", indent, imports.add(Optional.class),
                        imports.add(StreamProvider.class));
                p.format("%sreturn %s.createStream(contextPath, this);\n", indent.right(),
                        imports.add(RequestHelper.class));
                p.format("%s}\n", indent.left());
            }

            // write Patched class
            writePatchedClass(t, simpleClassName, imports, indent, p);

            // write toString
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

            p.format("%s}\n", indent.left());

            writeToFile(imports, w, t.getClassFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printJsonIncludeNonNull(Imports imports, PrintWriter p) {
        p.format("@%s(%s.NON_NULL)\n", imports.add(JsonInclude.class), imports.add(Include.class));
    }

    private void writePatchedClass(EntityType t, String simpleClassName, Imports imports, Indent indent,
            PrintWriter p) {
        p.format("\n%spublic static final class %s extends %s implements %s<%s> {\n", indent, CLASS_NAME_PATCHED,
                simpleClassName, imports.add(Patchable.class), simpleClassName);

        // write Patched constructor
        indent.right();
        writeConstructorSignature(t, CLASS_NAME_PATCHED, imports, indent, p);
        indent.right();
        String superFields = t.getFields(imports) //
                .stream() //
                .map(f -> ", " + f.fieldName) //
                .collect(Collectors.joining());
        p.format("%ssuper(contextPath, changedFields, unmappedFields, odataType%s);\n", indent, superFields);
        p.format("%s}\n", indent.left());

        // write patch() method
        writePutOrPatchMethod(t, simpleClassName, imports, indent, p, true);

        // write put method
        writePutOrPatchMethod(t, simpleClassName, imports, indent, p, false);

        p.format("%s}\n", indent.left());
    }

    private void writePutOrPatchMethod(EntityType t, String simpleClassName, Imports imports, Indent indent,
            PrintWriter p, boolean isPatch) {
        String methodName = isPatch ? "patch" : "put";
        p.format("\n%s@%s\n", indent, imports.add(Override.class));
        p.format("%spublic %s %s() {\n", indent, simpleClassName, methodName);
        String params = t.getFields(imports) //
                .stream() //
                .map(f -> ", " + f.fieldName) //
                .collect(Collectors.joining());

        p.format("%s%s.%s(this, contextPath, %s.EMPTY,  %s.INSTANCE);\n", indent.right(),
                imports.add(RequestHelper.class), methodName, imports.add(RequestOptions.class),
                imports.add(t.getFullClassNameSchema()));
        p.format("%s// pass null for changedFields to reset it\n", indent);
        p.format("%sreturn new %s(contextPath, null, unmappedFields, odataType%s);\n", indent, simpleClassName, params);
        p.format("%s}\n", indent.left());
    }

    private static void addChangedFieldsField(Imports imports, Indent indent, PrintWriter p) {
        p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
        p.format("%sprotected final %s changedFields;\n", indent, imports.add(ChangedFields.class));
    }

    private void writeConstructorSignature(EntityType t, String simpleClassName, Imports imports, Indent indent,
            PrintWriter p) {
        String parameterIndent = indent + Indent.INDENT + Indent.INDENT;

        String props = t.getFields(imports) //
                .stream() //
                .map(f -> String.format("@%s(\"%s\") %s %s", //
                        imports.add(JsonProperty.class), //
                        f.propertyName, //
                        f.importedType, //
                        f.fieldName)) //
                .map(x -> {
                    return ",\n" + parameterIndent + x;
                }) //
                .collect(Collectors.joining());

        p.format("\n%s@%s", indent, imports.add(JsonCreator.class));
        p.format(
                "\n%sprotected %s(\n%s@%s %s contextPath, \n%s@%s %s changedFields, \n%s@%s %s unmappedFields, \n%s@%s(\"%s\") %s odataType%s) {\n", //
                indent, //
                simpleClassName, //
                parameterIndent, //
                imports.add(JacksonInject.class), //
                imports.add(ContextPath.class), //
                parameterIndent, //
                imports.add(JacksonInject.class), //
                imports.add(ChangedFields.class), //
                parameterIndent, //
                imports.add(JacksonInject.class), //
                imports.add(UnmappedFields.class), //
                parameterIndent, //
                imports.add(JsonProperty.class), //
                "@odata.type", //
                imports.add(String.class), //
                props);
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
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", t.getPackage());
            p.format("IMPORTSHERE");

            printJsonIncludeNonNull(imports, p);
            printPropertyOrder(imports, p, t.getProperties());
            p.format("public class %s%s implements %s {\n\n", simpleClassName, t.getExtendsClause(imports),
                    imports.add(HasUnmappedFields.class));

            indent.right();
            if (!t.hasBaseType()) {
                addContextPathField(imports, indent, p);
            }

            addUnmappedFieldsField(imports, indent, p);

            // write fields from properties
            printPropertyFields(imports, indent, p, t.getProperties(), t.hasBaseType());

            // add constructor
            // build constructor parameters
            String props = t.getFields(imports) //
                    .stream() //
                    .map(f -> String.format("@%s(\"%s\") %s %s", imports.add(JsonProperty.class), //
                            f.propertyName, //
                            f.importedType, //
                            f.fieldName)) //
                    .map(x -> ",\n" + Indent.INDENT + Indent.INDENT + Indent.INDENT + x) //
                    .collect(Collectors.joining());

            // write constructor
            p.format("\n%s@%s", indent, imports.add(JsonCreator.class));
            p.format("\n%spublic %s(@%s %s contextPath, @%s %s unmappedFields, @%s(\"%s\") %s odataType%s) {\n", //
                    indent, //
                    simpleClassName, //
                    imports.add(JacksonInject.class), //
                    imports.add(ContextPath.class), //
                    imports.add(JacksonInject.class), //
                    imports.add(UnmappedFields.class), //
                    imports.add(JsonProperty.class), //
                    "@odata.type", //
                    imports.add(String.class), //
                    props);
            if (t.getBaseType() != null) {
                String superFields = t.getSuperFields(imports) //
                        .stream() //
                        .map(f -> ", " + f.fieldName) //
                        .collect(Collectors.joining());
                p.format("%ssuper(contextPath, unmappedFields, odataType%s);\n", indent.right(), superFields);
                indent.left();
            }
            indent.right();
            if (!t.hasBaseType()) {
                p.format("%sthis.contextPath = contextPath;\n", indent);
            }
            p.format("%sthis.unmappedFields = unmappedFields;\n", indent);
            if (t.getBaseType() == null) {
                p.format("%sthis.odataType = odataType;\n", indent);
            }
            // print constructor field assignments
            t.getProperties() //
                    .forEach(x -> {
                        String fieldName = Names.getIdentifier(x.getName());
                        p.format("%sthis.%s = %s;\n", indent, fieldName, fieldName);
                        if (isCollection(x) && !names.isEntityWithNamespace(names.getType(x))) {
                            p.format("%sthis.%sNextLink = %sNextLink;\n", indent, fieldName, fieldName);
                        }
                    });
            p.format("%s}\n", indent.left());

            printPropertyGetterAndSetters(imports, indent, p, simpleClassName, t.getFullType(), t.getProperties(),
                    t.getFields(imports), false);

            addUnmappedFieldsSetterAndGetter(imports, indent, p);

            writeBuilder(t, simpleClassName, imports, indent, p);

            p.format("\n}\n");
            writeToFile(imports, w, t.getClassFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEntityRequest(Schema schema, TEntityType entityType) {
        EntityType t = new EntityType(entityType, names);
        names.getDirectoryEntityRequest(schema).mkdirs();
        // TODO only write out those requests needed
        String simpleClassName = t.getSimpleClassNameEntityRequest();
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", t.getPackageEntityRequest());
            p.format("IMPORTSHERE");

            p.format("@%s\n", imports.add(JsonIgnoreType.class));
            p.format("public final class %s extends %s {\n\n", simpleClassName,
                    imports.add(EntityRequest.class) + "<" + imports.add(t.getFullClassNameEntity()) + ">");

            indent.right();

            // add constructor
            p.format("%spublic %s(%s contextPath) {\n", indent, simpleClassName, imports.add(ContextPath.class),
                    imports.add(String.class));
            p.format("%ssuper(%s.class, contextPath, %s.INSTANCE);\n", //
                    indent.right(), //
                    imports.add(t.getFullClassNameEntity()), //
                    imports.add(names.getFullClassNameSchema(schema)));
            p.format("%s}\n", indent.left());

            indent.left();

            // TODO also support navigation properties with complexTypes?
            t.getNavigationProperties() //
                    .stream() //
                    .filter(x -> names.isEntityWithNamespace(names.getInnerType(names.getType(x)))) //
                    .forEach(x -> {
                        indent.right();
                        final String returnClass;
                        String y = x.getType().get(0);
                        Schema sch = names.getSchema(names.getInnerType(y));
                        if (y.startsWith(COLLECTION_PREFIX)) {
                            String inner = names.getInnerType(y);
                            returnClass = imports.add(CollectionPageEntityRequest.class) + "<"
                                    + imports.add(names.getFullClassNameFromTypeWithNamespace(inner)) + ", "
                                    + imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, inner))
                                    + ">";
                        } else {
                            returnClass = imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, y));
                        }
                        p.format("\n%spublic %s %s() {\n", //
                                indent, //
                                returnClass, //
                                Names.getGetterMethodWithoutGet(x.getName()));
                        if (isCollection(x)) {
                            p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                            p.format("%scontextPath.addSegment(\"%s\"),\n", indent.right().right().right().right(),
                                    x.getName());
                            p.format("%s%s.class,\n", indent, imports.add(
                                    names.getFullClassNameFromTypeWithNamespace(names.getInnerType(names.getType(x)))));
                            p.format("%scontextPath -> new %s(contextPath), %s.INSTANCE);\n", indent,
                                    imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                            names.getInnerType(names.getType(x)))),
                                    imports.add(names.getFullClassNameSchema(sch)));
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

    private KeyInfo getKeyInfo(EntityType et, Imports imports) {
        KeyElement key = et.getFirstKey();

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
                        return String.format("new %s(\"%s\", %s)", imports.add(NameValue.class), z.getName(),
                                z.getFieldName());
                    } else {
                        return String.format("new %s(%s.toString())", imports.add(NameValue.class), z.getFieldName());
                    }
                }) //
                .collect(Collectors.joining(", "));
        addKeys = ".addKeys(" + addKeys + ")";
        return new KeyInfo(typedParams, addKeys);
    }

    private void writeContainer(Schema schema, TEntityContainer t) {
        names.getDirectoryContainer(schema).mkdirs();
        String simpleClassName = names.getSimpleClassNameContainer(schema, t.getName());
        Imports imports = new Imports(simpleClassName);
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
            p.format("public final class %s%s {\n\n", simpleClassName, extension);

            // TODO handle container extension

            // write fields
            p.format("%sprivate final %s contextPath;\n\n", indent.right(), imports.add(ContextPath.class));

            // write constructor
            p.format("%spublic %s(%s context) {\n", indent, simpleClassName, imports.add(Context.class));
            p.format("%sthis.contextPath = new %s(context, context.service().getBasePath());\n", indent.right(),
                    imports.add(ContextPath.class));
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
                        Schema sch = names.getSchema(x.getEntityType());
                        p.format("\n%spublic %s %s() {\n", indent, toType(x, imports),
                                Names.getIdentifier(x.getName()));
                        p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                        p.format("%scontextPath.addSegment(\"%s\"),\n", indent.right().right().right().right(),
                                x.getName());
                        p.format("%s%s.class,\n", indent,
                                imports.add(names.getFullClassNameFromTypeWithNamespace(x.getEntityType())));
                        p.format("%scontextPath -> new %s(contextPath), %s.INSTANCE);\n", indent,
                                imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                        x.getEntityType())), //
                                imports.add(names.getFullClassNameSchema(sch)));
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

            Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TSingleton.class) //
                    .forEach(x -> {
                        String importedType = toType(x, imports);
                        p.format("\n%spublic %s %s() {\n", indent, importedType, Names.getIdentifier(x.getName()));
                        p.format("%sreturn new %s(contextPath.addSegment(\"%s\"));\n", indent.right(), importedType,
                                x.getName());
                        p.format("%s}\n", indent.left());
                    });

            p.format("\n}\n");
            File classFile = names.getClassFileContainer(schema, t.getName());
            writeToFile(imports, w, classFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCollectionRequest(Schema schema, TEntityType entityType) {
        EntityType t = new EntityType(entityType, names);
        names.getDirectoryCollectionRequest(schema).mkdirs();
        String simpleClassName = names.getSimpleClassNameCollectionRequest(schema, t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageCollectionRequest(schema));
            p.format("IMPORTSHERE");
            p.format("public final class %s extends %s<%s, %s>{\n\n", simpleClassName,
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
                    imports.add(names.getFullClassNameSchema(schema)));
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
                                    indent, // tional`
                                    imports.add(names.getFullClassNameCollectionRequestFromTypeWithNamespace(sch, y)), //
                                    x.getName());

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
        p.format("%sprivate %s changedFields = %s.EMPTY;\n", indent, imports.add(ChangedFields.class),
                imports.add(ChangedFields.class));

        p.format("\n%sBuilder() {\n", indent);
        p.format("%s// prevent instantiation\n", indent.right());
        p.format("%s}\n", indent.left());

        // write builder setters

        fields.forEach(f -> {
            p.format("\n%spublic Builder %s(%s %s) {\n", indent, f.fieldName, f.importedType, f.fieldName);
            p.format("%sthis.%s = %s;\n", indent.right(), f.fieldName, f.fieldName);
            p.format("%sthis.changedFields = changedFields.add(\"%s\");\n", indent, f.name);
            p.format("%sreturn this;\n", indent);
            p.format("%s}\n", indent.left());
        });

        p.format("\n%spublic %s build() {\n", indent, simpleClassName);
        String builderProps = fields //
                .stream() //
                .map(f -> ", " + f.fieldName) //
                .collect(Collectors.joining());
        if (t instanceof EntityType) {
            p.format("%sreturn new %s(null, changedFields, %s.EMPTY, \"%s\"%s);\n", //
                    indent.right(), //
                    simpleClassName, //
                    imports.add(UnmappedFields.class), //
                    t.getFullType(), //
                    builderProps);
        } else {
            // exclude changedFields parameter
            p.format("%sreturn new %s(null, %s.EMPTY, \"%s\"%s);\n", //
                    indent.right(), //
                    simpleClassName, //
                    imports.add(UnmappedFields.class), //
                    t.getFullType(), //
                    builderProps);
        }
        p.format("%s}\n", indent.left());

        p.format("%s}\n", indent.left());
    }

    private static void addUnmappedFieldsField(Imports imports, Indent indent, PrintWriter p) {
        p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
        p.format("%sprotected %s unmappedFields;\n", indent, imports.add(UnmappedFields.class));
    }

    private static void addUnmappedFieldsSetterAndGetter(Imports imports, Indent indent, PrintWriter p) {
        p.format("\n%s@%s\n", indent, imports.add(JsonAnySetter.class));
        // TODO protect "setUnmappedField" name against clashes
        p.format("%sprivate void setUnmappedField(String name, Object value) {\n", indent);
        p.format("%sif (unmappedFields == null) {\n", indent.right());
        p.format("%sunmappedFields = new %s();\n", indent.right(), imports.add(UnmappedFields.class));
        p.format("%s}\n", indent.left());
        p.format("%sunmappedFields.put(name, value);\n", indent);
        p.format("%s}\n", indent.left());

        p.format("\n%s@%s\n", indent, imports.add(Override.class));
        p.format("%spublic %s getUnmappedFields() {\n", indent, imports.add(UnmappedFields.class));
        p.format("%sreturn unmappedFields == null ? %s.EMPTY : unmappedFields;\n", indent.right(),
                imports.add(UnmappedFields.class));
        p.format("%s}\n", indent.left());
    }

    private static void addContextPathInjectableField(Imports imports, Indent indent, PrintWriter p) {
        // add context path field
        p.format("\n%s@%s\n", indent, imports.add(JsonIgnore.class));
        addContextPathField(imports, indent, p);
    }

    private static void addContextPathField(Imports imports, Indent indent, PrintWriter p) {
        p.format("%sprotected final %s contextPath;\n", indent, imports.add(ContextPath.class));
    }

    private void printPropertyGetterAndSetters(Imports imports, Indent indent, PrintWriter p, String simpleClassName,
            String fullType, List<TProperty> properties, List<Field> fields, boolean ofEntity) {

        // write getters and setters
        properties //
                .forEach(x -> {
                    String fieldName = Names.getIdentifier(x.getName());
                    String t = names.getType(x);
                    boolean isCollection = isCollection(x);
                    if (isCollection) {
                        String inner = names.getInnerType(t);
                        String importedInnerType = names.toImportedTypeNonCollection(inner, imports);
                        boolean isEntity = names.isEntityWithNamespace(inner);
                        Class<?> collectionCls;
                        if (isEntity) {
                            collectionCls = CollectionPageEntity.class;
                        } else {
                            collectionCls = CollectionPageNonEntity.class;
                        }
                        p.format("\n%spublic %s<%s> %s() {\n", indent, imports.add(collectionCls), importedInnerType,
                                Names.getGetterMethod(x.getName()));
                        if (isEntity) {
                            Schema sch = names.getSchema(names.getInnerType(t));
                            p.format("%sreturn %s.from(contextPath.context(), %s, %s.class, %s.INSTANCE);\n",
                                    indent.right(), imports.add(CollectionPageEntity.class), fieldName,
                                    importedInnerType, imports.add(names.getFullClassNameSchema(sch)));
                        } else {
                            p.format("%sreturn new %s<%s>(contextPath, %s.class, %s, %sNextLink);\n", indent.right(),
                                    imports.add(CollectionPageNonEntity.class), importedInnerType, importedInnerType,
                                    fieldName, fieldName);
                        }
                        p.format("%s}\n", indent.left());
                    } else {
                        boolean isStream = "Edm.Stream".equals(names.getType(x));
                        if (isStream) {
                            p.format("\n%spublic %s<%s> %s() {\n", indent, imports.add(Optional.class),
                                    imports.add(StreamProvider.class), Names.getGetterMethod(x.getName()));
                            p.format("%sreturn %s.createStreamForEdmStream(contextPath, this, \"%s\", %s);\n",
                                    indent.right(), imports.add(RequestHelper.class), x.getName(), fieldName);
                            p.format("%s}\n", indent.left());
                            // TODO how to patch streamed content?
                        } else {
                            final String importedType = names.toImportedTypeNonCollection(t, imports);
                            String importedTypeWithOptional = imports.add(Optional.class) + "<" + importedType + ">";
                            p.format("\n%spublic %s %s() {\n", indent, importedTypeWithOptional,
                                    Names.getGetterMethod(x.getName()));
                            p.format("%sreturn %s.ofNullable(%s);\n", indent.right(), imports.add(Optional.class),
                                    fieldName);
                            p.format("%s}\n", indent.left());

                            String classSuffix = ofEntity ? "." + CLASS_NAME_PATCHED : "";
                            p.format("\n%spublic %s%s %s(%s %s) {\n", indent, simpleClassName, classSuffix,
                                    Names.getWithMethod(x.getName()), importedType, fieldName);
                            if (x.isUnicode() != null && !x.isUnicode()) {
                                p.format("%s%s.checkIsAscii(%s);\n", indent.right(),
                                        imports.add(EntityPreconditions.class), fieldName, fieldName);
                                indent.left();
                            }

                            // prepare parameters to constructor to return immutable copy
                            String params = fields.stream() //
                                    .map(f -> f.fieldName) //
                                    .map(a -> ", " + a) //
                                    .collect(Collectors.joining());
                            if (ofEntity) {
                                params = "contextPath, changedFields.add(\"" + x.getName() + "\")"
                                        + ", unmappedFields, "
                                        + imports.add(com.github.davidmoten.odata.client.Util.class)
                                        + ".nvl(odataType, \"" + fullType + "\")" + params;
                            } else {
                                params = "contextPath" + ", unmappedFields, odataType" + params;
                            }

                            indent.right();
                            p.format("%sreturn new %s%s(%s);\n", indent, simpleClassName, classSuffix, params);
                            p.format("%s}\n", indent.left());
                        }
                    }

                });

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
            p.format("%sprotected final %s %s;\n", indent, imports.add(String.class), "odataType");
        }
        properties.stream().forEach(x -> {
            p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), x.getName());
            p.format("%sprotected final %s %s;\n", indent, names.toImportedType(x, imports),
                    Names.getIdentifier(x.getName()));
            String t = names.getInnerType(names.getType(x));
            if (isCollection(x) && !names.isEntityWithNamespace(t)) {
                p.format("\n%s@%s(\"%s@nextLink\")\n", indent, imports.add(JsonProperty.class), x.getName());
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
                    p.format("\n%spublic %s %s() {\n", indent, typeName, Names.getGetterMethod(x.getName()));
                    if (isCollection(x)) {
                        if (names.isEntityWithNamespace(names.getType(x))) {
                            Schema sch = names.getSchema(names.getInnerType(names.getType(x)));
                            p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                            p.format("%scontextPath.addSegment(\"%s\"),\n", indent.right().right().right().right(),
                                    x.getName());
                            p.format("%s%s.class,\n", indent, imports.add(
                                    names.getFullClassNameFromTypeWithNamespace(names.getInnerType(names.getType(x)))));
                            p.format("%scontextPath -> new %s(contextPath), %s.INSTANCE);\n", indent,
                                    imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                                            names.getInnerType(names.getType(x)))), //
                                    imports.add(names.getFullClassNameSchema(sch)));
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

    private String toType(TNavigationProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        if (!isCollection(x)) {
            if (x.isNullable() != null && x.isNullable()) {
                String r = names.toType(t, imports, List.class);
                return imports.add(Optional.class) + "<" + r + ">";
            } else {
                // is navigation property so must be an entity and is a single request
                Schema sch = names.getSchema(names.getInnerType(t));
                return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, t));
            }
        } else {
            return names.toType(t, imports, CollectionPageEntityRequest.class);
        }
    }

    private String toType(TSingleton x, Imports imports) {
        String t = x.getType();
        if (!isCollection(x.getType())) {
            Schema sch = names.getSchema(names.getInnerType(t));
            return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(sch, t));
        } else {
            return names.toType(t, imports, CollectionPageEntityRequest.class);
        }
    }

    private String toType(TEntitySet x, Imports imports) {
        String t = x.getEntityType();
        // an entity set is always a collection
        return names.wrapCollection(imports, CollectionPageEntityRequest.class, t);
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
