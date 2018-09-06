package com.github.davidmoten.odata.client;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Preconditions;

public final class Generator {

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

        // write actions

        // write functions

        // consume annotations

    }

    private void writeComplexType(TComplexType t) {
        Imports imports = new Imports();
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageComplexType());
            p.format("IMPORTSHERE");
            String simpleClassName = names.getSimpleClassNameComplexType(t.getName());
            p.format("public final class %s {\n\n", simpleClassName);

            // write fields from properties
            indent.right();
            printProperties(imports, indent, p, simpleClassName, t.getPropertyOrNavigationPropertyOrAnnotation());

            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileComplexType(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEntity(TEntityType t) {
        Imports imports = new Imports();
        Indent indent = new Indent();

        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", names.getPackageEntity());
            p.format("IMPORTSHERE");
            String simpleClassName = names.getSimpleClassNameEntity(t.getName());
            final String extension;
            if (t.getBaseType() != null) {
                extension = " extends " + imports.add(names.getFullGeneratedClassNameFromNamespacedType(t.getBaseType()));
            } else {
                extension = "";
            }
            p.format("public %sclass %s%s {\n\n", t.isAbstract() ? "abstract " : "", simpleClassName, extension);

            // write fields from properties
            indent.right();
            printProperties(imports, indent, p, simpleClassName, t.getKeyOrPropertyOrNavigationProperty());

            p.format("\n}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileEntity(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printProperties(Imports imports, Indent indent, PrintWriter p, String simpleClassName,
            List<Object> properties) {
        Util.filter(properties, TProperty.class) //
                .forEach(x -> {
                    p.format("%sprivate %s %s;\n", indent, toType(x, imports), Names.getIdentifier(x.getName()));
                });

        // write getters and setters
        Util.filter(properties, TProperty.class) //
                .forEach(x -> {
                    String fieldName = Names.getIdentifier(x.getName());
                    String typeName = toType(x, imports);
                    p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), x.getName());
                    p.format("%spublic %s %s() {\n", indent, typeName, Names.getGetterMethod(x.getName()));
                    p.format("%sreturn %s;\n", indent.right(), fieldName);
                    p.format("%s}\n", indent.left());

                    p.format("\n%s@%s(\"%s\")\n", indent, imports.add(JsonProperty.class), x.getName());
                    p.format("%spublic %s %s(%s %s) {\n", indent, simpleClassName, Names.getSetterMethod(x.getName()),
                            typeName, fieldName);
                    p.format("%sthis.%s = %s;\n", indent.right(), fieldName, fieldName);
                    p.format("%sreturn this;\n", indent);
                    p.format("%s}\n", indent.left());
                });
    }

    private String toType(TProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        if (x.isNullable() && !isCollection(x)) {
            String r = toType(t, false, imports);
            return imports.add(Optional.class) + "<" + r + ">";
        } else {
            return toType(t, true, imports);
        }
    }

    private static boolean isCollection(TProperty x) {
        return isCollection(x.getType().get(0));
    }

    private static boolean isCollection(String t) {
        return t.startsWith("Collection(") && t.endsWith(")");
    }

    private String toType(String t, boolean canUsePrimitive, Imports imports) {
        if (t.startsWith("Edm.")) {
            return toTypeFromEdm(t, canUsePrimitive, imports);
        } else if (t.startsWith(schema.getNamespace())) {
            return imports.add(names.getFullGeneratedClassNameFromNamespacedType(t));
        } else if (isCollection(t)) {
            String inner = t.substring("Collection(".length(), t.length() - 1);
            return imports.add(List.class) + "<" + toType(inner, false, imports) + ">";
        } else {
            throw new RuntimeException("unhandled type: " + t);
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
        Imports imports = new Imports();
        Indent indent = new Indent();
        try {
            String simpleClassName = names.getSimpleClassNameEnum(t.getName());
            StringWriter w = new StringWriter();
            try (PrintWriter p = new PrintWriter(w)) {
                p.format("package %s;\n\n", names.getPackageEnum());
                p.format("IMPORTSHERE\n");
                p.format("public enum %s implements %s {\n", simpleClassName, imports.add(Enum.class));

                // add members
                indent.right();
                String s = Util.filter(t.getMemberOrAnnotation(), TEnumTypeMember.class) //
                        .map(x -> String.format("%s%s(\"%s\", \"%s\")", indent, Names.toConstant(x.getName()),
                                x.getName(), x.getValue()))
                        .collect(Collectors.joining(",\n"));
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
            Files.write(names.getClassFileEnum(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
