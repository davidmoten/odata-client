package com.github.davidmoten.odata.client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;

final class Names {

    private static final Set<String> javaReservedWords = Sets.newHashSet("abstract", "assert",
            "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "extends", "false", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private", "protected", "public", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "void", "volatile", "while");

    private final Options options;
    private final File output;

    private final Map<String, String> classNamesFromNamespacedType;
    private final Schema schema;

    private Names(Schema schema, Options options) {
        this.schema = schema;
        this.options = options;
        File output = new File(options.outputDirectory());
        Util.deleteDirectory(output);
        output.mkdirs();
        this.output = output;
        this.classNamesFromNamespacedType = createMap(schema, options);
    }

    // factory method
    static Names clearOutputDirectoryAndCreate(Schema schema, Options options) {
        Names names = new Names(schema, options);
        names.getDirectoryEntity().mkdirs();
        names.getDirectoryEnum().mkdirs();
        names.getDirectoryComplexType().mkdirs();
        names.getDirectoryCollectionRequest().mkdirs();
        names.getDirectoryContainer().mkdirs();
        names.getDirectoryEntityRequest().mkdirs();
        return names;
    }

    private Map<String, String> createMap(Schema schema, Options options) {
        Map<String, String> map = new HashMap<>();
        Util.types(schema, TEnumType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                        getFullClassNameEnum(x.getName())));

        Util.types(schema, TEntityType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                        getFullClassNameEntity(x.getName())));

        Util.types(schema, TComplexType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                        getFullClassNameComplexType(x.getName())));
        return map;
    }

    static String toSimpleClassName(String name) {
        return upperFirst(name);
    }

    private static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    private static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }

    static String toConstant(String name) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return name.replaceAll(regex, replacement).toUpperCase();
    }

    static String getIdentifier(String s) {
        if (javaReservedWords.contains(s)) {
            s = s + "_";
        }
        return lowerFirst(s);
    }

    static String getGetterMethod(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        return "get" + upperFirst(name);
    }

    static String getSetterMethod(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        return "set" + upperFirst(name);
    }

    private static File toDirectory(File base, String pkg) {
        String path = base.getAbsolutePath() + File.separatorChar
                + pkg.replace('.', File.separatorChar);
        return new File(path);
    }

    File getDirectoryEntity() {
        return toDirectory(output, options.pkg() + options.packageSuffixEntity());
    }

    File getDirectoryContainer() {
        return toDirectory(output, options.pkg() + options.packageSuffixContainer());
    }

    File getDirectoryEnum() {
        return toDirectory(output, options.pkg() + options.packageSuffixEnum());
    }

    File getDirectoryComplexType() {
        return toDirectory(output, options.pkg() + options.packageSuffixComplexType());
    }

    File getDirectoryCollectionRequest() {
        return toDirectory(output, options.pkg() + options.packageSuffixCollectionRequest());
    }

    File getDirectoryEntityRequest() {
        return toDirectory(output, options.pkg() + options.packageSuffixEntityRequest());
    }

    String getPackageEnum() {
        return options.pkg() + options.packageSuffixEnum();
    }

    String getPackageEntity() {
        return options.pkg() + options.packageSuffixEntity();
    }

    String getPackageCollectionRequest() {
        return options.pkg() + options.packageSuffixCollectionRequest();
    }

    String getPackageEntityRequest() {
        return options.pkg() + options.packageSuffixEntityRequest();
    }

    String getPackageComplexType() {
        return options.pkg() + options.packageSuffixComplexType();
    }

    String getPackageContainer() {
        return options.pkg() + options.packageSuffixContainer();
    }

    String getSimpleClassNameEnum(String name) {
        return Names.toSimpleClassName(name);
    }

    String getSimpleClassNameEntity(String name) {
        return Names.toSimpleClassName(name);
    }

    String getSimpleClassNameContainer(String name) {
        return Names.toSimpleClassName(name);
    }

    String getSimpleClassNameCollectionRequest(String name) {
        return Names.toSimpleClassName(name + options.collectionRequestClassSuffix());
    }

    String getSimpleClassNameEntityRequest(String name) {
        return Names.toSimpleClassName(name + options.entityRequestClassSuffix());
    }

    String getSimpleClassNameComplexType(String name) {
        return Names.toSimpleClassName(name);
    }

    String getFullClassNameEnum(String name) {
        return getPackageEnum() + "." + getSimpleClassNameEnum(name);
    }

    String getFullClassNameEntity(String name) {
        return getPackageEntity() + "." + getSimpleClassNameEntity(name);
    }

    String getFullClassNameEntityRequest(String name) {
        return getPackageEntityRequest() + "." + getSimpleClassNameEntityRequest(name);
    }

    private String getFullClassNameComplexType(String name) {
        return getPackageComplexType() + "." + getSimpleClassNameComplexType(name);
    }

    public String getFullClassNameCollectionRequestFromTypeWithNamespace(String name) {
        String simple = getLastItemInDotDelimitedString(name);
        return getPackageCollectionRequest() + "." + upperFirst(simple)
                + options.collectionRequestClassSuffix();
    }

    public String getFullClassNameEntityRequestFromTypeWithNamespace(String name) {
        String simple = getLastItemInDotDelimitedString(name);
        return getPackageEntityRequest() + "." + upperFirst(simple)
                + options.entityRequestClassSuffix();
    }

    public String getSimpleTypeNameFromTypeWithNamespace(String name) {
        return getLastItemInDotDelimitedString(name);
    }

    private static String getLastItemInDotDelimitedString(String name) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1, name.length());
        }
    }

    File getClassFileEnum(String name) {
        return new File(getDirectoryEnum(), getSimpleClassNameEnum(name) + ".java");
    }

    File getClassFileComplexType(String name) {
        return new File(getDirectoryComplexType(), getSimpleClassNameComplexType(name) + ".java");
    }

    File getClassFileEntity(String name) {
        return new File(getDirectoryEntity(), getSimpleClassNameEntity(name) + ".java");
    }

    File getClassFileContainer(String name) {
        return new File(getDirectoryContainer(), getSimpleClassNameContainer(name) + ".java");
    }

    File getClassFileCollectionRequest(String name) {
        return new File(getDirectoryCollectionRequest(),
                getSimpleClassNameCollectionRequest(name) + ".java");
    }

    File getClassFileEntityRequest(String name) {
        return new File(getDirectoryEntityRequest(),
                getSimpleClassNameEntityRequest(name) + ".java");
    }

    String getFullGeneratedClassNameFromTypeWithNamespace(String type) {
        return Preconditions.checkNotNull(classNamesFromNamespacedType.get(type),
                "class name not found for " + type);
    }

    String getFullGeneratedClassNameFromTypeWithoutNamespace(String type) {
        return Preconditions.checkNotNull(
                classNamesFromNamespacedType.get(schema.getNamespace() + "." + type),
                "class name not found for " + type);
    }

    public String getInnerType(String name) {
        if (name.startsWith("Collection(")) {
            return name.substring("Collection(".length(), name.length() - 1);
        } else {
            return name;
        }
    }

}
