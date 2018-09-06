package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;
import org.oasisopen.odata.csdl.v4.TProperty;

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
                .peek(x -> System.out.println(x.getName())) //
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
            p.format("IMPORTSHERE\n");
            String simpleClassName = names.getSimpleClassNameComplexType(t.getName());
            p.format("public class %s {\n", simpleClassName);

            // write fields from properties
            indent.right();
            Util.filter(t.getPropertyOrNavigationPropertyOrAnnotation(), TProperty.class) //
                    .forEach(x -> {
                        p.format("%sprivate %s %s;\n", indent, toType(x, imports), Names.toIdentifier(x.getName()));
                    });
            p.format("}\n");
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
            p.format("IMPORTSHERE\n");
            String simpleClassName = names.getSimpleClassNameEntity(t.getName());
            p.format("public class %s {\n", simpleClassName);

            // write fields from properties
            indent.right();
            Util.filter(t.getKeyOrPropertyOrNavigationProperty(), TProperty.class).forEach(x -> {
                p.format("%sprivate %s %s;\n", indent, toType(x, imports), Names.toIdentifier(x.getName()));
            });

            p.format("}\n");
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
            Files.write(names.getClassFileEntity(t.getName()).toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toType(TProperty x, Imports imports) {
        Preconditions.checkArgument(x.getType().size() == 1);
        String t = x.getType().get(0);
        return toType(t, !x.isNullable(), imports);
    }

    private String toType(String t, boolean canUsePrimitive, Imports imports) {
        if (t.startsWith("Edm.")) {
            return toTypeFromEdm(t, canUsePrimitive, imports);
        } else if (t.startsWith(schema.getNamespace())) {
            return names.getFullGeneratedClassNameFromNamespacedType(t);
        } else if (t.startsWith("Collection(")) {
            String inner = t.substring("Collection(".length(), t.length() - 1);
            return imports.add(List.class) + "<" + toType(inner, false, imports) + ">";
        } else {
            return "Unknown_" + t;
        }
    }

    private String toTypeFromEdm(String t, boolean canUsePrimitive, Imports imports) {
        final String result;
        if (t.equals("Edm.String")) {
            result = String.class.getCanonicalName();
        } else if (t.equals("Edm.Boolean")) {
            if (canUsePrimitive) {
                result = Boolean.class.getCanonicalName();
            } else {
                result = boolean.class.getCanonicalName();
            }
        } else if (t.equals("Edm.DateTimeOffset")) {
            result = OffsetDateTime.class.getCanonicalName();
        } else if (t.equals("Edm.Int32")) {
            if (canUsePrimitive) {
                result = int.class.getCanonicalName();
            } else {
                result = Integer.class.getCanonicalName();
            }
        } else if (t.equals("Edm.Guid")) {
            result = String.class.getCanonicalName();
        } else {
            result = "Unknown_" + t;
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
