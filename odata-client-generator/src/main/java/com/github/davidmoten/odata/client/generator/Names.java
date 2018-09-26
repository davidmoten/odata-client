package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.edm.GeographyPoint;
import com.github.davidmoten.odata.client.edm.UnsignedByte;

public final class Names {

    private static final Set<String> javaReservedWords = Sets.newHashSet("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while");

    private static final String COLLECTION_PREFIX = "Collection(";

    private final Options options;
    private final File output;

    private final Map<String, String> classNamesFromNamespacedType;
    private final Schema schema;

    private final Map<String, String> entityClassNamesFromNamespacedType;

    private Names(Schema schema, Options options) {
        this.schema = schema;
        this.options = options;
        File pkgDirectory = toDirectory(new File(options.outputDirectory()), options.pkg());
        Util.deleteDirectory(pkgDirectory);
        pkgDirectory.mkdirs();
        this.output = new File(options.outputDirectory());
        this.classNamesFromNamespacedType = createMap(schema, options);
        this.entityClassNamesFromNamespacedType = createEntityMap(schema, options);
    }

    // factory method
    public static Names clearOutputDirectoryAndCreate(Schema schema, Options options) {
        Names names = new Names(schema, options);
        names.getDirectoryEntity().mkdirs();
        names.getDirectoryEnum().mkdirs();
        names.getDirectoryComplexType().mkdirs();
        names.getDirectoryCollectionRequest().mkdirs();
        names.getDirectoryContainer().mkdirs();
        names.getDirectoryEntityRequest().mkdirs();
        names.getDirectorySchema().mkdirs();
        return names;
    }

    private Map<String, String> createMap(Schema schema, Options options) {
        Map<String, String> map = new HashMap<>();
        Util.types(schema, TEnumType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(), getFullClassNameEnum(x.getName())));

        Util.types(schema, TEntityType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(), getFullClassNameEntity(x.getName())));

        Util.types(schema, TComplexType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                        getFullClassNameComplexType(x.getName())));
        return map;
    }

    private Map<String, String> createEntityMap(Schema schema, Options options) {
        Map<String, String> map = new HashMap<>();
        Util.types(schema, TEntityType.class) //
                .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(), getFullClassNameEntity(x.getName())));
        return map;
    }

    public static String toSimpleClassName(String name) {
        return upperFirst(name);
    }

    private static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    private static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }

    public static String toConstant(String name) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return name.replaceAll(regex, replacement).toUpperCase();
    }

    public static String getIdentifier(String s) {
        if (javaReservedWords.contains(s.toLowerCase())) {
            return s.toLowerCase() + "_";
        } else if (s.toUpperCase().equals(s)) {
            return s;
        } else {
            return lowerFirst(s);
        }
    }

    public static String getGetterMethod(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        return "get" + upperFirst(name);
    }

    public static String getSetterMethod(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        return "with" + upperFirst(name);
    }

    private static File toDirectory(File base, String pkg) {
        String path = base.getAbsolutePath() + File.separatorChar + pkg.replace('.', File.separatorChar);
        return new File(path);
    }

    public Schema getSchema() {
        return schema;
    }

    public File getDirectorySchema() {
        return toDirectory(output, options.pkg() + options.packageSuffixSchema());
    }

    public File getDirectoryEntity() {
        return toDirectory(output, options.pkg() + options.packageSuffixEntity());
    }

    public File getDirectoryContainer() {
        return toDirectory(output, options.pkg() + options.packageSuffixContainer());
    }

    public File getDirectoryEnum() {
        return toDirectory(output, options.pkg() + options.packageSuffixEnum());
    }

    public File getDirectoryComplexType() {
        return toDirectory(output, options.pkg() + options.packageSuffixComplexType());
    }

    public File getDirectoryCollectionRequest() {
        return toDirectory(output, options.pkg() + options.packageSuffixCollectionRequest());
    }

    public File getDirectoryEntityRequest() {
        return toDirectory(output, options.pkg() + options.packageSuffixEntityRequest());
    }

    public String getPackageSchema() {
        return options.pkg() + options.packageSuffixSchema();
    }

    public String getPackageEnum() {
        return options.pkg() + options.packageSuffixEnum();
    }

    public String getPackageEntity() {
        return options.pkg() + options.packageSuffixEntity();
    }

    public String getPackageCollectionRequest() {
        return options.pkg() + options.packageSuffixCollectionRequest();
    }

    public String getPackageEntityRequest() {
        return options.pkg() + options.packageSuffixEntityRequest();
    }

    public String getPackageComplexType() {
        return options.pkg() + options.packageSuffixComplexType();
    }

    public String getPackageContainer() {
        return options.pkg() + options.packageSuffixContainer();
    }

    public String getSimpleClassNameEnum(String name) {
        return Names.toSimpleClassName(name);
    }

    public String getSimpleClassNameEntity(String name) {
        return Names.toSimpleClassName(name);
    }

    public String getSimpleClassNameContainer(String name) {
        return Names.toSimpleClassName(name);
    }

    public String getSimpleClassNameCollectionRequest(String name) {
        return Names.toSimpleClassName(name + options.collectionRequestClassSuffix());
    }

    public String getSimpleClassNameEntityRequest(String name) {
        return Names.toSimpleClassName(name + options.entityRequestClassSuffix());
    }

    public String getSimpleClassNameComplexType(String name) {
        return Names.toSimpleClassName(name);
    }

    public String getSimpleClassNameSchema() {
        return options.simpleClassNameSchema();
    }

    public String getFullClassNameSchema() {
        return getPackageSchema() + "." + getSimpleClassNameSchema();
    }

    public String getFullClassNameEnum(String name) {
        return getPackageEnum() + "." + getSimpleClassNameEnum(name);
    }

    public String getFullClassNameEntity(String name) {
        return getPackageEntity() + "." + getSimpleClassNameEntity(name);
    }

    public String getFullClassNameEntityRequest(String name) {
        return getPackageEntityRequest() + "." + getSimpleClassNameEntityRequest(name);
    }

    private String getFullClassNameComplexType(String name) {
        return getPackageComplexType() + "." + getSimpleClassNameComplexType(name);
    }

    public String getFullClassNameCollectionRequestFromTypeWithNamespace(String name) {
        String simple = getLastItemInDotDelimitedString(name);
        return getPackageCollectionRequest() + "." + upperFirst(simple) + options.collectionRequestClassSuffix();
    }

    public String getFullClassNameEntityRequestFromTypeWithNamespace(String name) {
        String simple = getLastItemInDotDelimitedString(name);
        return getFullClassNameEntityRequestFromTypeWithoutNamespace(simple);
    }

    public String getFullClassNameEntityRequestFromTypeWithoutNamespace(String name) {
        return getPackageEntityRequest() + "." + upperFirst(name) + options.entityRequestClassSuffix();
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

    public File getClassFileSchema() {
        return new File(getDirectorySchema(), getSimpleClassNameSchema() + ".java");
    }

    public File getClassFileEnum(String name) {
        return new File(getDirectoryEnum(), getSimpleClassNameEnum(name) + ".java");
    }

    public File getClassFileComplexType(String name) {
        return new File(getDirectoryComplexType(), getSimpleClassNameComplexType(name) + ".java");
    }

    public File getClassFileEntity(String name) {
        return new File(getDirectoryEntity(), getSimpleClassNameEntity(name) + ".java");
    }

    public File getClassFileContainer(String name) {
        return new File(getDirectoryContainer(), getSimpleClassNameContainer(name) + ".java");
    }

    public File getClassFileCollectionRequest(String name) {
        return new File(getDirectoryCollectionRequest(), getSimpleClassNameCollectionRequest(name) + ".java");
    }

    public File getClassFileEntityRequest(String name) {
        return new File(getDirectoryEntityRequest(), getSimpleClassNameEntityRequest(name) + ".java");
    }

    public String getFullClassNameFromTypeWithNamespace(String type) {
        return Preconditions.checkNotNull(classNamesFromNamespacedType.get(type), "class name not found for " + type);
    }

    public String getFullClassNameFromTypeWithoutNamespace(String type) {
        return Preconditions.checkNotNull(classNamesFromNamespacedType.get(schema.getNamespace() + "." + type),
                "class name not found for " + type);
    }

    public String getInnerType(String name) {
        if (name.startsWith("Collection(")) {
            return name.substring("Collection(".length(), name.length() - 1);
        } else {
            return name;
        }
    }

    public boolean isEntityWithNamespace(String name) {
        return entityClassNamesFromNamespacedType.keySet().contains(getInnerType(name));
    }

    public static String getGetterMethodWithoutGet(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        return lowerFirst(name);
    }

    public String getType(TProperty x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException(
                    "property " + x.getName() + "must have one and only one type but was: " + x.getType());
        }
        return list.get(0);
    }

    public String getType(TNavigationProperty x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException(
                    "property " + x.getName() + "must have one and only one type but was: " + x.getType());
        }
        return list.get(0);
    }

    public String getFullTypeFromSimpleType(String name) {
        return schema.getNamespace() + "." + name;
    }

    public boolean isCollection(TProperty x) {
        return isCollection(getType(x));
    }

    private static boolean isCollection(String t) {
        return t.startsWith(COLLECTION_PREFIX) && t.endsWith(")");
    }

    public String toImportedType(TProperty x, Imports imports) {
        return toType(getType(x), imports, List.class);
    }

    public String toType(String t, Imports imports, Class<?> collectionClass) {
        if (t.startsWith("Edm.")) {
            return toTypeFromEdm(t, imports);
        } else if (t.startsWith(schema.getNamespace())) {
            return imports.add(getFullClassNameFromTypeWithNamespace(t));
        } else if (isCollection(t)) {
            String inner = getInnerType(t);
            return wrapCollection(imports, collectionClass, inner);
        } else {
            throw new RuntimeException("unhandled type: " + t);
        }
    }

    public String toTypeFromEdm(String t, Imports imports) {
        if (t.equals("Edm.String")) {
            return imports.add(String.class);
        } else if (t.equals("Edm.Boolean")) {
            return imports.add(Boolean.class);
        } else if (t.equals("Edm.DateTimeOffset")) {
            return imports.add(OffsetDateTime.class);
        } else if (t.equals("Edm.Duration")) {
            return imports.add(Duration.class);
        } else if (t.equals("Edm.TimeOfDay")) {
            return imports.add(LocalTime.class);
        } else if (t.equals("Edm.Date")) {
            return imports.add(LocalDate.class);
        } else if (t.equals("Edm.Int32")) {
            return imports.add(Integer.class);
        } else if (t.equals("Edm.Int16")) {
            return imports.add(Short.class);
        } else if (t.equals("Edm.Byte")) {
            return imports.add(UnsignedByte.class);
        } else if (t.equals("Edm.SByte")) {
            return byte.class.getCanonicalName();
        } else if (t.equals("Edm.Single")) {
            return imports.add(Float.class);
        } else if (t.equals("Edm.Double")) {
            return imports.add(Double.class);
        } else if (t.equals("Edm.Guid")) {
            return imports.add(String.class);
        } else if (t.equals("Edm.Int64")) {
            return imports.add(Long.class);
        } else if (t.equals("Edm.Binary")) {
            return "byte[]";
        } else if (t.equals("Edm.Stream")) {
            return imports.add(InputStream.class);
        } else if (t.equals("Edm.GeographyPoint")) {
            return imports.add(GeographyPoint.class);
        } else if (t.equals("Edm.Decimal")) {
            return imports.add(BigDecimal.class);
        } else {
            throw new RuntimeException("unhandled type: " + t);
        }
    }

    String wrapCollection(Imports imports, Class<?> collectionClass, String inner) {
        if (collectionClass.equals(CollectionPageEntityRequest.class)) {
            // get the type without namespace
            String entityRequestClass = getFullClassNameEntityRequestFromTypeWithNamespace(inner);
            String a = toType(inner, imports, collectionClass);
            return imports.add(collectionClass) + "<" + a + ", " + imports.add(entityRequestClass) + ">";
        } else {
            return imports.add(collectionClass) + "<" + toType(inner, imports, collectionClass) + ">";
        }
    }
}
