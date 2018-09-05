package com.github.davidmoten.odata.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;

public final class Generator {

    private final Options options;
    private final Schema schema;

    private static final String PKG_SUFFIX_ENUM = ".enums";

    public Generator(Options options, Schema schema) {
        this.options = options;
        this.schema = schema;
    }

    public void generate() {
        File output = new File(options.outputDirectory());
        Util.deleteDirectory(output);
        output.mkdirs();

        // write enums
        schema.getComplexTypeOrEntityTypeOrTypeDefinition() //
                .stream() //
                .filter(x -> x instanceof TEnumType) //
                .map(x -> ((TEnumType) x)) //
                .forEach(x -> writeEnum(output, PKG_SUFFIX_ENUM, x));

        // write entityTypes

        // write complexTypes

        // write actions

        // write functions

        // consume annotations

    }

    private void writeEnum(File output, String pkgSuffix, TEnumType t) {
        Imports imports = new Imports();
        Indent indent = new Indent();
        String pkg = options.pkg() + pkgSuffix;
        File dir = toDirectory(output, options.pkg() + pkgSuffix);
        dir.mkdirs();
        String simpleClassName = toSimpleClassName(t.getName());
        File file = new File(dir, simpleClassName + ".java");
        try {
            file.createNewFile();
            StringWriter w = new StringWriter();
            try (PrintWriter p = new PrintWriter(w)) {
                p.format("package %s;\n\n", pkg);
                p.format("IMPORTSHERE\n");
                p.format("public enum %s implements %s {\n", simpleClassName, imports.add(Enum.class));

                // add members
                indent.add();
                String s = t.getMemberOrAnnotation() //
                        .stream() //
                        .filter(x -> x instanceof TEnumTypeMember) //
                        .map(x -> ((TEnumTypeMember) x)) //
                        .map(x -> String.format("%s%s(\"%s\",\"%s\")", indent, x.getName().toUpperCase(), x.getName(),
                                x.getValue()))
                        .collect(Collectors.joining(",\n"));
                indent.minus();
                p.format("\n%s;\n\n", s);

                // add fields
                p.format("%sprivate final %s name;\n", indent.add(), imports.add(String.class));
                p.format("%sprivate final %s value;\n\n", indent, imports.add(String.class));

                // add constructor
                p.format("%sprivate %s(%s name, %s value) {\n", indent, simpleClassName, imports.add(String.class),
                        imports.add(String.class));
                p.format("%sthis.name = name;\n", indent.add());
                p.format("%sthis.value = value;\n", indent);
                p.format("%s}\n\n", indent.minus());
                
                // add methods
                p.format("%s@%s\n", indent, imports.add(Override.class));
                p.format("%spublic %s enumName() {\n", indent, imports.add(String.class));
                p.format("%sreturn name;\n", indent.add());
                p.format("%s}\n\n", indent.minus());
                
                p.format("%s@%s\n", indent, imports.add(Override.class));
                p.format("%spublic %s enumValue() {\n",indent,  imports.add(String.class));
                p.format("%sreturn value;\n", indent.add());
                p.format("%s}\n\n", indent.minus());
                
                // close class
                p.format("}\n");
            }
            byte[] bytes = w.toString().replace("IMPORTSHERE", imports.toString()).getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String toSimpleClassName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    private static File toDirectory(File base, String pkg) {
        String path = base.getAbsolutePath() + File.separatorChar + pkg.replace('.', File.separatorChar);
        return new File(path);
    }

}
