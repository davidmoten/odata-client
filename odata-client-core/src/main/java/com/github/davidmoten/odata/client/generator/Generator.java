package com.github.davidmoten.odata.client.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.CollectionEntityRequestOptions;
import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.EntityPreconditions;
import com.github.davidmoten.odata.client.EntityRequest;
import com.github.davidmoten.odata.client.EntityRequestOptions;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.RequestHelper;
import com.github.davidmoten.odata.client.UnsignedByte;

public final class Generator {

    private static final String COLLECTION_PREFIX = "Collection(";
    private final Schema schema;
    private final Names names;

    public Generator(Options options, Schema schema) {
        this.schema = schema;
        this.names = Names.clearOutputDirectoryAndCreate(schema, options);
    }

    public void generate() {

        // write enums
        Util.types(schema, TEnumType.class) //
                .forEach(x -> writeEnum(x));

        // write entityTypes
        Util.types(schema, TEntityType.class) //
                .forEach(x -> writeEntity(x));

        // write complexTypes
        Util.types(schema, TComplexType.class) //
                .forEach(x -> writeComplexType(x));

        // write collection requests
        Util.types(schema, TEntityType.class) //
                .forEach(x -> writeCollectionRequest(x));

        // write containers
        Util.types(schema, TEntityContainer.class) //
                .forEach(x -> writeContainer(x));

        // write single requests
        Util.types(schema, TEntityType.class) //
                .forEach(x -> writeEntityRequest(x));

        // write actions

        // write functions

        // consume annotations

    }

    private void writeEntityRequest(TEntityType t) {
        // TODO only write out those requests needed
        String simpleClassName = names.getSimpleClassNameEntityRequest(t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageEntityRequest());
            p.format("IMPORTSHERE");

            p.format("public final class %s implements %s {\n\n", simpleClassName,
                    imports.add(EntityRequest.class) + "<"
                            + imports.add(names.getFullClassNameEntity(t.getName())) + ">");

            // add field
            p.format("%sprivate final %s contextPath;\n\n", indent.right(),
                    imports.add(ContextPath.class));
            p.format("%sprivate final %s id;\n\n", indent, imports.add(String.class));

            // add constructor
            p.format("%spublic %s(%s contextPath, %s id) {\n", indent, simpleClassName,
                    imports.add(ContextPath.class), imports.add(String.class));
            p.format("%sthis.contextPath = contextPath;\n", indent.right());
            p.format("%sthis.id = id;\n", indent);
            p.format("%s}\n\n", indent.left());

            // write get
            p.format("%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic %s get(%s<%s> options) {\n", indent, //
                    imports.add(names.getFullClassNameFromTypeWithoutNamespace(t.getName())), //
                    imports.add(EntityRequestOptions.class),
                    imports.add(names.getFullClassNameEntity(t.getName())));
            p.format("%sreturn %s.get(contextPath, %s.class, id, options);\n", indent.right(),
                    imports.add(RequestHelper.class),
                    imports.add(names.getFullClassNameFromTypeWithoutNamespace(t.getName())));
            p.format("%s}\n", indent.left());

            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic void delete(%s<%s> options) {\n", indent, //
                    imports.add(EntityRequestOptions.class),
                    imports.add(names.getFullClassNameEntity(t.getName())));
            p.format("%s}\n", indent);

            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic %s update(%s<%s> options) {\n", indent, //
                    imports.add(names.getFullClassNameFromTypeWithoutNamespace(t.getName())), //
                    imports.add(EntityRequestOptions.class),
                    imports.add(names.getFullClassNameEntity(t.getName())));
            p.format("%sreturn null;\n", indent.right());
            p.format("%s}\n", indent.left());

            p.format("\n%s@%s\n", indent, imports.add(Override.class));
            p.format("%spublic %s patch(%s<%s> options) {\n", indent, //
                    imports.add(names.getFullClassNameFromTypeWithoutNamespace(t.getName())), //
                    imports.add(EntityRequestOptions.class),
                    imports.add(names.getFullClassNameEntity(t.getName())));
            p.format("%sreturn null;\n", indent.right());
            p.format("%s}\n", indent.left());
            indent.left();

            Util.filter(t.getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class) //
                    .filter(x -> names
                            .isEntityWithNamespace(names.getInnerType(x.getType().get(0)))) //
                    .forEach(x -> {
                        indent.right();
                        final String returnClass;
                        String y = x.getType().get(0);
                        if (y.startsWith(COLLECTION_PREFIX)) {
                            String inner = names.getInnerType(y);
                            returnClass = imports.add(CollectionPageEntityRequest.class) + "<"
                                    + imports
                                            .add(names.getFullClassNameFromTypeWithNamespace(inner))
                                    + ", "
                                    + imports.add(names
                                            .getFullClassNameEntityRequestFromTypeWithNamespace(
                                                    inner))
                                    + ">";
                        } else {
                            returnClass = imports.add(
                                    names.getFullClassNameEntityRequestFromTypeWithNamespace(y));
                        }
                        p.format("\n%spublic %s %s() {\n", indent, //
                                returnClass, //
                                Names.getGetterMethodWithoutGet(x.getName()));
                        p.format("%sreturn null;\n", indent.right());
                        p.format("%s}\n", indent.left());
                        if (y.startsWith(COLLECTION_PREFIX)) {
                            // TODO use actual key name from metadata
                            String inner = names.getInnerType(y);
                            if (names.isEntityWithNamespace(inner)) {
                                String entityRequestType = names
                                        .getFullClassNameEntityRequestFromTypeWithNamespace(inner);
                                p.format("\n%spublic %s %s(%s id) {\n", indent,
                                        imports.add(entityRequestType),
                                        Names.getIdentifier(x.getName()),
                                        imports.add(String.class));
                                p.format("%sreturn new %s(contextPath.addSegment(\"%s\"), id);\n",
                                        indent.right(), imports.add(entityRequestType),
                                        x.getName());
                                p.format("%s}\n", indent.left());
                            } else {
                                p.format("\n%spublic %s %s(%s id) {\n", indent, //
                                        imports.add(names
                                                .getFullClassNameEntityRequestFromTypeWithNamespace(
                                                        inner)), //
                                        Names.getGetterMethodWithoutGet(x.getName()),
                                        imports.add(String.class));
                                p.format("%sreturn null;\n", indent.right());
                                p.format("%s}\n", indent.left());
                            }
                        }
                        indent.left();
                    });
            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileEntityRequest(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeContainer(TEntityContainer t) {
        String simpleClassName = names.getSimpleClassNameContainer(t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageContainer());
            p.format("IMPORTSHERE");

            final String extension;
            if (t.getExtends() != null) {
                extension = " extends "
                        + imports.add(names.getFullClassNameFromTypeWithNamespace(t.getExtends()));
            } else {
                extension = "";
            }
            p.format("public final class %s%s {\n\n", simpleClassName, extension);

            // write fields
            p.format("%sprivate final %s contextPath;\n\n", indent.right(),
                    imports.add(ContextPath.class));

            // write constructor
            p.format("%spublic %s(%s context) {\n", indent, simpleClassName,
                    imports.add(Context.class));
            p.format("%sthis.contextPath = new %s(context, context.service().getBasePath());\n",
                    indent.right(), imports.add(ContextPath.class));
            p.format("%s}\n", indent.left());

            // write get methods from properties
            Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
                    .forEach(x -> {
                        p.format("\n%spublic %s %s() {\n", indent, toType(x, imports),
                                Names.getIdentifier(x.getName()));
                        p.format("%sreturn new %s(\n", indent.right(), toType(x, imports));
                        p.format("%scontextPath.addSegment(\"%s\"),\n",
                                indent.right().right().right().right(), x.getName());
                        p.format("%s(contextPath, id) -> new %s(contextPath, id));\n", indent,
                                imports.add(
                                        names.getFullClassNameEntityRequestFromTypeWithNamespace(
                                                x.getEntityType())));
                        p.format("%s}\n", indent.left().left().left().left().left());

                        if (names.isEntityWithNamespace(x.getEntityType())) {
                            String entityRequestType = names
                                    .getFullClassNameEntityRequestFromTypeWithNamespace(
                                            x.getEntityType());
                            p.format("\n%spublic %s %s(%s id) {\n", indent,
                                    imports.add(entityRequestType),
                                    Names.getIdentifier(x.getName()), imports.add(String.class));
                            p.format("%sreturn new %s(contextPath.addSegment(\"%s\"), id);\n",
                                    indent.right(), imports.add(entityRequestType), x.getName());
                            p.format("%s}\n", indent.left());
                        }
                    });

            Util.filter(t.getEntitySetOrActionImportOrFunctionImport(), TSingleton.class) //
                    .forEach(x -> {
                        p.format("\n%spublic %s %s() {\n", indent, toType(x, imports),
                                Names.getIdentifier(x.getName()));
                        p.format("%sreturn null;\n", indent.right());
                        p.format("%s}\n", indent.left());
                    });

            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileContainer(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCollectionRequest(TEntityType t) {
        String simpleClassName = names.getSimpleClassNameCollectionRequest(t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageCollectionRequest());
            p.format("IMPORTSHERE");
            p.format("public final class %s {\n\n", simpleClassName);

            // write fields from properties
            indent.right();
            p.format("%spublic %s<%s> get(%s options) {\n", indent, //
                    imports.add(CollectionPage.class), //
                    imports.add(names.getFullClassNameFromTypeWithoutNamespace(t.getName())), //
                    imports.add(CollectionEntityRequestOptions.class));
            p.format("%sreturn null;\n", indent.right());
            p.format("%s}\n", indent.left());

            Util.filter(t.getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class) //
                    .forEach(n -> {
                        p.println();
                        if (n.getType().get(0).startsWith(COLLECTION_PREFIX)) {
                            p.format("%spublic %s %s() {\n", indent,
                                    names.getFullClassNameCollectionRequestFromTypeWithNamespace(
                                            names.getInnerType(n.getType().get(0))), //
                                    names.getSimpleTypeNameFromTypeWithNamespace(n.getName()));
                            p.format("%sreturn null;\n", indent.right());
                            p.format("%s}\n", indent.left());
                        }
                    });
            indent.left();
            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileCollectionRequest(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void writeComplexType(TComplexType t) {
        String simpleClassName = names.getSimpleClassNameComplexType(t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageComplexType());
            p.format("IMPORTSHERE");
            p.format("public final class %s {\n\n", simpleClassName);

            // write fields from properties
            indent.right();
            printPropertyFields(imports, indent, p,
                    t.getPropertyOrNavigationPropertyOrAnnotation());
            printPropertyGetterAndSetters(imports, indent, p, simpleClassName,
                    t.getPropertyOrNavigationPropertyOrAnnotation());

            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileComplexType(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEntity(TEntityType t) {
        String simpleClassName = names.getSimpleClassNameEntity(t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageEntity());
            p.format("IMPORTSHERE");
            final String extension;
            if (t.getBaseType() != null) {
                extension = " extends "
                        + imports.add(names.getFullClassNameFromTypeWithNamespace(t.getBaseType()));
            } else {
                extension = "";
            }
            p.format("@%s(%s.NON_NULL)\n", imports.add(JsonInclude.class),
                    imports.add(Include.class));
            p.format("public %sclass %s%s implements %s {\n\n", t.isAbstract() ? "abstract " : "",
                    simpleClassName, extension, imports.add(ODataEntity.class));

            // write fields from properties

            // add context path field
            p.format("%sprivate final %s contextPath;\n\n", indent.right(),
                    imports.add(ContextPath.class));

            // add other fields
            printPropertyFields(imports, indent, p, t.getKeyOrPropertyOrNavigationProperty());

            p.format("%sprivate %s<%s,%s> unmappedFields = new %s<%s, %s>();\n", indent,
                    imports.add(Map.class), imports.add(String.class), imports.add(String.class),
                    imports.add(HashMap.class), imports.add(String.class),
                    imports.add(String.class));

            // add constructor
            p.format("\n%spublic %s(%s contextPath) {\n", indent, simpleClassName,
                    imports.add(ContextPath.class));
            if (t.getBaseType() != null) {
                p.format("%ssuper(contextPath);\n", indent.right());
            }
            p.format("%sthis.contextPath = contextPath;\n", indent);
            p.format("%s}\n", indent.left());

            printPropertyGetterAndSetters(imports, indent, p, simpleClassName,
                    t.getKeyOrPropertyOrNavigationProperty());
            printNavigationPropertyGetters(imports, indent, p,
                    t.getKeyOrPropertyOrNavigationProperty());

            p.format("\n\n%s@%s\n", indent, imports.add(JsonAnySetter.class));
            // TODO protect "other" name against clashes
            p.format("%spublic void setUnmappedField(String name, String value) {\n", indent);
            p.format("%sunmappedFields.put(name, value);\n", indent.right());
            p.format("%s}\n", indent.left());

            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString())
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileEntity(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printPropertyGetterAndSetters(Imports imports, Indent indent, PrintWriter p,
            String simpleClassName, List<Object> properties) {

        // write getters and setters
        Util.filter(properties, TProperty.class) //
                .forEach(x -> {
                    String fieldName = Names.getIdentifier(x.getName());
                    String typeName = toType(x, imports);
                    p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class),
                            x.getName());
                    p.format("%spublic %s %s() {\n", indent, typeName,
                            Names.getGetterMethod(x.getName()));
                    if (x.isNullable() && !isCollection(x)) {
                        p.format("%sreturn %s.of(%s);\n", indent.right(),
                                imports.add(Optional.class), fieldName);
                    } else {
                        p.format("%sreturn %s;\n", indent.right(), fieldName);
                    }
                    p.format("%s}\n", indent.left());

                    p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class),
                            x.getName());
                    p.format("%spublic %s %s(%s %s) {\n", indent, simpleClassName,
                            Names.getSetterMethod(x.getName()), typeName, fieldName);
                    if (x.isUnicode() != null && !x.isUnicode()) {
                        p.format("%s%s.checkIsAscii(%s);\n", indent.right(),
                                imports.add(EntityPreconditions.class), fieldName, fieldName);
                        indent.left();
                    }
                    if (x.isNullable() && !isCollection(x)) {
                        p.format("%sthis.%s = %s.orElse(null);\n", indent.right(), fieldName,
                                fieldName);
                    } else {
                        p.format("%sthis.%s = %s;\n", indent.right(), fieldName, fieldName);
                    }
                    p.format("%sreturn this;\n", indent);
                    p.format("%s}\n", indent.left());
                });
    }

    private void printPropertyFields(Imports imports, Indent indent, PrintWriter p,
            List<Object> properties) {
        Util.filter(properties, TProperty.class) //
                .forEach(x -> {
                    p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class),
                            x.getName());
                    p.format("%sprivate %s %s;\n", indent, toTypeSuppressUseOfOptional(x, imports),
                            Names.getIdentifier(x.getName()));
                });
    }

    private void printNavigationPropertyGetters(Imports imports, Indent indent, PrintWriter p,
            List<Object> properties) {
        Class<TNavigationProperty> cls = TNavigationProperty.class;

        // write getters and setters
        Util.filter(properties, cls) //
                .forEach(x -> {
                    String typeName = toType(x, imports);
                    p.format("%spublic %s %s() {\n", indent, typeName,
                            Names.getGetterMethod(x.getName()));
                    p.format("%sreturn null;\n", indent.right());
                    p.format("%s}\n", indent.left());
                });
    }

    private String toType(TNavigationProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        if (!isCollection(x)) {
            if (x.isNullable() != null && x.isNullable()) {
                String r = toType(t, false, imports, List.class);
                return imports.add(Optional.class) + "<" + r + ">";
            } else {
                // is navigation property so must be an entity and is a single request
                return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(t));
            }
        } else {
            return toType(t, true, imports, CollectionPageEntityRequest.class);
        }
    }

    private String toType(TSingleton x, Imports imports) {
        String t = x.getType();
        if (!isCollection(x.getType())) {
            return imports.add(names.getFullClassNameEntityRequestFromTypeWithNamespace(t));
        } else {
            return toType(t, true, imports, CollectionPageEntityRequest.class);
        }
    }

    private String toType(TEntitySet x, Imports imports) {
        String t = x.getEntityType();
        // an entity set is always a collection
        return wrapCollection(imports, CollectionPageEntityRequest.class, t);
    }

    private String toType(TProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        if (x.isNullable() && !isCollection(x)) {
            String r = toType(t, false, imports, List.class);
            return imports.add(Optional.class) + "<" + r + ">";
        } else {
            return toType(t, true, imports, List.class);
        }
    }

    private String toTypeSuppressUseOfOptional(TProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        if (x.isNullable() && !isCollection(x)) {
            return toType(t, false, imports, List.class);
        } else {
            return toType(t, true, imports, List.class);
        }
    }

    private static boolean isCollection(TProperty x) {
        return isCollection(x.getType().get(0));
    }

    private static boolean isCollection(TNavigationProperty x) {
        return isCollection(x.getType().get(0));
    }

    private static boolean isCollection(String t) {
        return t.startsWith(COLLECTION_PREFIX) && t.endsWith(")");
    }

    private String toType(String t, boolean canUsePrimitive, Imports imports,
            Class<?> collectionClass) {
        if (t.startsWith("Edm.")) {
            return toTypeFromEdm(t, canUsePrimitive, imports);
        } else if (t.startsWith(schema.getNamespace())) {
            return imports.add(names.getFullClassNameFromTypeWithNamespace(t));
        } else if (isCollection(t)) {
            String inner = t.substring(COLLECTION_PREFIX.length(), t.length() - 1);
            return wrapCollection(imports, collectionClass, inner);
        } else {
            throw new RuntimeException("unhandled type: " + t);
        }
    }

    private String wrapCollection(Imports imports, Class<?> collectionClass, String inner) {
        if (collectionClass.equals(CollectionPageEntityRequest.class)) {
            // get the type without namespace
            String entityRequestClass = names
                    .getFullClassNameEntityRequestFromTypeWithNamespace(inner);
            String a = toType(inner, false, imports, collectionClass);
            return imports.add(collectionClass) + "<" + a + ", " + imports.add(entityRequestClass)
                    + ">";
        } else {
            return imports.add(collectionClass) + "<"
                    + toType(inner, false, imports, collectionClass) + ">";
        }
    }

    private String toTypeFromEdm(String t, boolean canUsePrimitive, Imports imports) {
        final String result;
        if (t.equals("Edm.String")) {
            result = imports.add(String.class);
        } else if (t.equals("Edm.Boolean")) {
            if (canUsePrimitive) {
                result = boolean.class.getCanonicalName();
            } else {
                result = imports.add(Boolean.class);
            }
        } else if (t.equals("Edm.DateTimeOffset")) {
            result = imports.add(OffsetDateTime.class);
        } else if (t.equals("Edm.Duration")) {
            result = imports.add(Duration.class);
        } else if (t.equals("Edm.TimeOfDay")) {
            result = imports.add(LocalTime.class);
        } else if (t.equals("Edm.Date")) {
            result = imports.add(LocalDate.class);
        } else if (t.equals("Edm.Int32")) {
            if (canUsePrimitive) {
                result = int.class.getCanonicalName();
            } else {
                result = imports.add(Integer.class);
            }
        } else if (t.equals("Edm.Int16")) {
            if (canUsePrimitive) {
                result = short.class.getCanonicalName();
            } else {
                result = imports.add(Short.class);
            }
        } else if (t.equals("Edm.Byte")) {
            result = imports.add(UnsignedByte.class);
        } else if (t.equals("Edm.SByte")) {
            if (canUsePrimitive) {
                result = byte.class.getCanonicalName();
            } else {
                result = imports.add(byte.class);
            }
        } else if (t.equals("Edm.Single")) {
            if (canUsePrimitive) {
                result = float.class.getCanonicalName();
            } else {
                result = imports.add(Float.class);
            }
        } else if (t.equals("Edm.Double")) {
            if (canUsePrimitive) {
                result = double.class.getCanonicalName();
            } else {
                result = imports.add(Double.class);
            }
        } else if (t.equals("Edm.Guid")) {
            result = imports.add(String.class);
        } else if (t.equals("Edm.Int64")) {
            return canUsePrimitive ? long.class.getCanonicalName() : imports.add(Long.class);
        } else if (t.equals("Edm.Binary")) {
            return "byte[]";
        } else if (t.equals("Edm.Stream")) {
            return imports.add(InputStream.class);
        } else {
            throw new RuntimeException("unhandled type: " + t);
        }
        return imports.add(result);
    }

    private void writeEnum(TEnumType t) {
        String simpleClassName = names.getSimpleClassNameEnum(t.getName());
        Imports imports = new Imports(simpleClassName);
        Indent indent = new Indent();
        try {
            StringWriter w = new StringWriter();
            try (PrintWriter p = new PrintWriter(w)) {
                p.format("package %s;\n\n", names.getPackageEnum());
                p.format("IMPORTSHERE\n");
                p.format("public enum %s implements %s {\n", simpleClassName,
                        imports.add(com.github.davidmoten.odata.client.Enum.class));

                // add members
                indent.right();
                String s = Util.filter(t.getMemberOrAnnotation(), TEnumTypeMember.class) //
                        .map(x -> String.format("%s%s(\"%s\", \"%s\")", indent,
                                Names.toConstant(x.getName()), x.getName(), x.getValue()))
                        .collect(Collectors.joining(",\n"));
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
            Files.write(names.getClassFileEnum(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
