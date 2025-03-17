package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TEnumTypeMember;
import org.oasisopen.odata.csdl.v4.TFunction;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.Sets;
import com.github.davidmoten.odata.client.CollectionPageEntityRequest;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.generator.model.EntityType;
import com.github.davidmoten.odata.client.generator.model.Property;
import com.github.davidmoten.odata.client.generator.model.Structure;
import com.github.davidmoten.odata.client.internal.EdmSchemaInfo;

public final class Names {

    private static final Set<String> javaReservedWords = Sets.newHashSet("abstract", "assert",
            "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "extends", "false", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private", "protected", "public", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "void", "volatile", "while", "var");
    
    private static final String COLLECTION_PREFIX = "Collection(";

    private final File output;

    private final List<Schema> schemas;

    private final Map<String, String> entityClassNamesFromNamespacedType;
    private final Map<String, String> classNamesFromNamespacedType;
    private final Map<String, TEntityType> entityTypesFromNamespacedType;

    private final Options opts;

    private final Map<Object, Schema> objectToSchema;

    private final Documentation docs;
    
    private final Map<NamedStructure, PropertyWithFieldName> structureProperties;

    private Names(List<Schema> schemas, Options opts) {
        this.schemas = schemas;
        this.opts = opts;
        this.output = new File(opts.getOutputDirectory());
        this.classNamesFromNamespacedType = createMap(schemas, opts);
        this.entityClassNamesFromNamespacedType = createEntityMap(schemas, opts);
        this.entityTypesFromNamespacedType = createEntityTypesMap(schemas, opts);
        this.objectToSchema = schemas.stream()
                .flatMap(s -> s.getComplexTypeOrEntityTypeOrTypeDefinition().stream()
                        .map(t -> new SchemaAndType<Object>(s, t))) //
                .collect(Collectors.toMap(st -> st.type, st -> st.schema));
        this.docs = new Documentation(schemas);
        this.structureProperties = new HashMap<>(); // lazily loaded
    }

    // factory method
    public static Names create(List<Schema> schemas, Options options) {
        return new Names(schemas, options);
    }

    public SchemaOptions getOptions(Schema schema) {
        return opts.getSchemaOptions(schema.getNamespace());
    }

    private Map<String, TEntityType> createEntityTypesMap(List<Schema> schemas2, Options opts2) {
        Map<String, TEntityType> map = new HashMap<>();
        for (Schema schema : schemas) {
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(), x));
        }
        return map;
    }

    private Map<String, String> createMap(List<Schema> schemas, Options options) {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemas) {
            Util.types(schema, TEnumType.class) //
                    .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                            getFullClassNameEnum(schema, x.getName())));

            Util.types(schema, TEntityType.class) //
                    .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                            getFullClassNameEntity(schema, x.getName())));

            Util.types(schema, TComplexType.class) //
                    .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                            getFullClassNameComplexType(schema, x.getName())));
        }
        return map;
    }

    private Map<String, String> createEntityMap(List<Schema> schemas, Options options) {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemas) {
            Util.types(schema, TEntityType.class) //
                    .forEach(x -> map.put(schema.getNamespace() + "." + x.getName(),
                            getFullClassNameEntity(schema, x.getName())));
        }
        return map;
    }

    public static String toSimpleClassName(String name) {
        return upperFirst(name);
    }

    private static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
    }

    private static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }

    public static String toConstant(String name) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return name.replaceAll(regex, replacement).toUpperCase(Locale.ENGLISH);
    }

    public static String getIdentifier(String s) {
        if (javaReservedWords.contains(s.toLowerCase(Locale.ENGLISH))) {
            return s.toLowerCase(Locale.ENGLISH) + "_";
        } else if (s.toUpperCase(Locale.ENGLISH).equals(s)) {
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

    public static String getWithMethod(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        return "with" + upperFirst(name);
    }

    private static File toDirectory(File base, String pkg) {
        String path = base.getAbsolutePath() + File.separatorChar
                + pkg.replace('.', File.separatorChar);
        return new File(path);
    }

    public String getSimpleClassNameContainer(String name) {
        return Names.toSimpleClassName(name);
    }

    public String getSimpleTypeNameFromTypeWithNamespace(String name) {
        return getLastItemInDotDelimitedString(name);
    }

    private static String getLastItemInDotDelimitedString(String name) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1);
        }
    }

    public String getFullClassNameFromTypeWithNamespace(String type) {
        return Preconditions.checkNotNull(classNamesFromNamespacedType.get(type),
                "class name not found for " + type);
    }

    public String getFullClassNameFromTypeWithoutNamespace(Schema schema, String type) {
        return Preconditions.checkNotNull(
                classNamesFromNamespacedType.get(schema.getNamespace() + "." + type),
                "class name not found for " + type);
    }

    public String getInnerType(String type) {
        if (type.startsWith("Collection(")) {
            return type.substring("Collection(".length(), type.length() - 1);
        } else {
            return type;
        }
    }

    public boolean isEntityWithNamespace(String type) {
        return entityClassNamesFromNamespacedType.containsKey(getInnerType(type));
    }

    public static String getGetterMethodWithoutGet(String name) {
        if (name.equalsIgnoreCase("class")) {
            name = "cls";
        }
        if (javaReservedWords.contains(name.toLowerCase(Locale.ENGLISH))) {
            name += "_";
        }
        return lowerFirst(name);
    }

    public String getType(TProperty x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException("property " + x.getName()
                    + "must have one and only one type but was: " + x.getType());
        }
        return list.get(0);
    }

    public String getType(TNavigationProperty x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException("property " + x.getName()
                    + "must have one and only one type but was: " + x.getType());
        }
        return list.get(0);
    }

    public String getType(TActionFunctionParameter x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException("property " + x.getName()
                    + "must have one and only one type but was: " + x.getType());
        }
        return list.get(0);
    }

    public boolean isCollection(TProperty x) {
        return isCollection(getType(x));
    }

    public boolean isCollection(TNavigationProperty x) {
        return isCollection(getType(x));
    }

    public static boolean isCollection(String t) {
        return t.startsWith(COLLECTION_PREFIX) && t.endsWith(")");
    }

    public String toImportedFullClassName(TProperty x, Imports imports) {
        return toImportedFullClassName(getType(x), imports, List.class);
    }
    
    public String toImportedFullClassName(TNavigationProperty x, Imports imports) {
        return toImportedFullClassName(getType(x), imports, List.class);
    }

    public String toImportedFullClassName(TActionFunctionParameter x, Imports imports) {
        return toImportedFullClassName(getType(x), imports, List.class);
    }

    public String toImportedTypeNonCollection(String t, Imports imports) {
        if (t.startsWith("Edm.")) {
            return getImportedFullClassNameFromEdm(t, imports);
        } else {
            return imports.add(getFullClassNameFromTypeWithNamespace(t));
        }
    }

    public String toImportedFullClassName(String t, Imports imports, Class<?> collectionClass) {
        if (t.startsWith("Edm.")) {
            return getImportedFullClassNameFromEdm(t, imports);
        } else if (isCollection(t)) {
            String inner = getInnerType(t);
            return wrapCollection(imports, collectionClass, inner);
        } else {
            return imports.add(getFullClassNameFromTypeWithNamespace(t));
        }
    }

    public String getImportedFullClassNameFromEdm(String t, Imports imports) {
        Class<?> cls = EdmSchemaInfo.INSTANCE.getClassFromTypeWithNamespace(t);
        if (cls.equals(byte[].class)) {
            return "byte[]";
        } else {
            return imports.add(cls);
        }
    }

    String wrapCollection(Imports imports, Class<?> collectionClass, String inner) {
        if (collectionClass.equals(CollectionPageEntityRequest.class)) {
            Schema sch = getSchema(inner);
            // get the type without namespace
            String entityRequestClass = getFullClassNameEntityRequestFromTypeWithNamespace(sch,
                    inner);
            String a = toImportedFullClassName(inner, imports, collectionClass);
            return imports.add(collectionClass) + "<" + a + ", " + imports.add(entityRequestClass)
                    + ">";
        } else {
            return imports.add(collectionClass) + "<"
                    + toImportedFullClassName(inner, imports, collectionClass) + ">";
        }
    }

    public String getSimpleClassNameSchema(Schema schema) {
        return getOptions(schema).simpleClassNameSchema;
    }

    public String getPackageSchema(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg + o.packageSuffixSchema;
    }

    public String getFullTypeFromSimpleType(Schema schema, String name) {
        return schema.getNamespace() + "." + name;
    }

    public String getFullClassNameEnum(Schema schema, String name) {
        return getPackageEnum(schema) + "." + getSimpleClassNameEnum(schema, name);
    }

    public String getFullClassNameEntity(Schema schema, String name) {
        return getPackageEntity(schema) + "." + getSimpleClassNameEntity(schema, name);
    }

    public String getFullClassNameComplexType(Schema schema, String name) {
        return getPackageComplexType(schema) + "." + getSimpleClassNameComplexType(schema, name);
    }

    public String getSimpleClassNameEntity(Schema schema, String name) {
        return Names.toSimpleClassName(name);
    }

    public String getPackageEntity(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixEntity();
    }

    public File getClassFileSchema(Schema schema) {
        return new File(getDirectorySchema(schema), getSimpleClassNameSchema(schema) + ".java");
    }

    public File getDirectorySchema(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixSchema());
    }

    public File getDirectoryEnum(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixEnum());
    }

    public String getSimpleClassNameEnum(Schema schema, String name) {
        return Names.toSimpleClassName(name);
    }

    public String getPackageEnum(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixEnum();
    }

    public File getClassFileEnum(Schema schema, String name) {
        return new File(getDirectoryEnum(schema), getSimpleClassNameEnum(schema, name) + ".java");
    }

    public File getDirectoryEntity(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixEntity());
    }

    static final class SchemaAndType<T> {
        final Schema schema;
        final T type;

        SchemaAndType(Schema schema, T type) {
            this.schema = schema;
            this.type = type;
        }
    }

    public Schema getSchema(TEntityType entityType) {
        return getSchemaFromType(entityType);
    }

    public Schema getSchema(TAction action) {
        return getSchemaFromType(action);
    }

    public Schema getSchema(TFunction function) {
        return getSchemaFromType(function);
    }

    private <T> Schema getSchemaFromType(Object type) {
        return objectToSchema.get(type);
    }

    public Schema getSchema(TComplexType complexType) {
        return getSchemaFromType(complexType);
    }

    public List<Schema> getSchemas() {
        return schemas;
    }

    public String getFullClassNameEntityRequestFromTypeWithNamespace(Schema schema, String name) {
        String simple = getLastItemInDotDelimitedString(name);
        return getFullClassNameEntityRequestFromTypeWithoutNamespace(schema, simple);
    }

    public String getFullClassNameEntityRequestFromTypeWithoutNamespace(Schema schema,
            String name) {
        SchemaOptions o = getOptions(schema);
        return getPackageEntityRequest(schema) + "." + upperFirst(name)
                + o.entityRequestClassSuffix();
    }

    public String getPackageEntityRequest(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixEntityRequest();
    }

    public File getClassFileComplexType(Schema schema, String name) {
        return new File(getDirectoryComplexType(schema),
                getSimpleClassNameComplexType(schema, name) + ".java");
    }

    public String getSimpleClassNameComplexType(Schema schema, String name) {
        return Names.toSimpleClassName(name);
    }

    public File getDirectoryComplexType(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixComplexType());
    }

    public File getClassFileEntity(Schema schema, String name) {
        return new File(getDirectoryEntity(schema),
                getSimpleClassNameEntity(schema, name) + ".java");
    }

    public String getPackageComplexType(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixComplexType();
    }

    public File getDirectoryEntityRequest(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixEntityRequest());
    }

    public String getSimpleClassNameEntityRequest(Schema schema, String name) {
        SchemaOptions o = getOptions(schema);
        return Names.toSimpleClassName(name + o.entityRequestClassSuffix());
    }

    String toTypeWithNamespace(Schema schema, String simpleType) {
        return schema.getNamespace() + "." + simpleType;
    }

    public Schema getSchema(String typeWithNamespace) {
        //noinspection RedundantTypeArguments
        return schemas //
                .stream() //
                .flatMap(s -> Util
                        .filter(s.getComplexTypeOrEntityTypeOrTypeDefinition(), TEntityType.class)
                        .map(t -> new SchemaAndType<TEntityType>(s, t))) //
                .filter(x -> toTypeWithNamespace(x.schema, x.type.getName())
                        .equals(typeWithNamespace)) //
                .map(x -> x.schema) //
                .findFirst() //
                .orElseGet(() -> schemas //
                        .stream() //
                        .flatMap(s -> Util
                                .filter(s.getComplexTypeOrEntityTypeOrTypeDefinition(),
                                        TComplexType.class)
                                .map(t -> new SchemaAndType<TComplexType>(s, t))) //
                        .filter(x -> toTypeWithNamespace(x.schema, x.type.getName())
                                .equals(typeWithNamespace)) //
                        .map(x -> x.schema) //
                        .findFirst() //
                        .orElseGet(() -> schemas //
                                .stream() //
                                .flatMap(s -> Util
                                        .filter(s.getComplexTypeOrEntityTypeOrTypeDefinition(),
                                                TEnumType.class)
                                        .map(t -> new SchemaAndType<TEnumType>(s, t))) //
                                .filter(x -> toTypeWithNamespace(x.schema, x.type.getName())
                                        .equals(typeWithNamespace)) //
                                .map(x -> x.schema) //
                                .findFirst() //
                                .<RuntimeException>orElseThrow(() -> {
                                    throw new RuntimeException(
                                            "type not found: " + typeWithNamespace);
                                })));
    }

    public File getClassFileEntityRequest(Schema schema, String name) {
        return new File(getDirectoryEntityRequest(schema),
                getSimpleClassNameEntityRequest(schema, name) + ".java");
    }

    public File getDirectoryContainer(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixContainer());
    }

    public String getSimpleClassNameContainer(Schema schema, String name) {
        return Names.toSimpleClassName(name);
    }

    public String getPackageContainer(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixContainer();
    }

    public File getClassFileContainer(Schema schema, String name) {
        return new File(getDirectoryContainer(schema),
                getSimpleClassNameContainer(schema, name) + ".java");
    }

    public File getDirectoryEntityCollectionRequest(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixEntityCollectionRequest());
    }

    public File getDirectoryComplexTypeCollectionRequest(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixComplexTypeCollectionRequest());
    }

    public String getSimpleClassNameCollectionRequest(Schema schema, String name) {
        SchemaOptions o = getOptions(schema);
        return Names.toSimpleClassName(name + o.collectionRequestClassSuffix());
    }

    public String getPackageCollectionRequest(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixEntityCollectionRequest();
    }

    public String getFullClassNameEntityRequest(Schema schema, String name) {
        return getPackageEntityRequest(schema) + "."
                + getSimpleClassNameEntityRequest(schema, name);
    }

    public String getFullClassNameCollectionRequestFromTypeWithNamespace(Schema schema,
            String name) {
        String simple = getLastItemInDotDelimitedString(name);
        SchemaOptions o = getOptions(schema);
        return getPackageCollectionRequest(schema) + "." + upperFirst(simple)
                + o.collectionRequestClassSuffix();
    }

    public File getClassFileEntityCollectionRequest(Schema schema, String name) {
        return new File(getDirectoryEntityCollectionRequest(schema),
                getSimpleClassNameCollectionRequest(schema, name) + ".java");
    }

    public File getClassFileComplexTypeCollectionRequest(Schema schema, String name) {
        return new File(getDirectoryComplexTypeCollectionRequest(schema),
                getSimpleClassNameCollectionRequest(schema, name) + ".java");
    }

    public EntityType getEntityType(String typeWithNamespace) {
        return new EntityType(getSchema(typeWithNamespace), entityTypesFromNamespacedType.get(typeWithNamespace), this);
    }

    public String getEnumInstanceName(TEnumType t, String name) {
        // ensures uniqueness
        //
        // maps enum names to enum instance names
        // e.g. theResponse -> THE_RESPONSE
        Map<String, String> map = new HashMap<>();
        Util.filter(t.getMemberOrAnnotation(), TEnumTypeMember.class) //
                .forEach(m -> {
                    String instanceName = Names.toConstant(m.getName());
                    if (!map.containsValue(instanceName)) {
                        map.put(m.getName(), instanceName);
                    } else {
                        int i = 1;
                        while (map.containsValue(instanceName + i)) {
                            i++;
                        }
                        map.put(m.getName(), instanceName + i);
                    }
                });
        return map.get(name);
    }

    public String getInnerType(TActionFunctionParameter x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException("property " + x.getName()
                    + "must have one and only one type but was: " + x.getType());
        }
        return getInnerType(list.get(0));
    }

    public boolean isCollection(TActionFunctionParameter x) {
        return isCollection(getType(x));
    }

    public String getType(TActionFunctionReturnType x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException(
                    "object must have one and only one type but was: " + x.getType());
        }
        return list.get(0);
    }

    public boolean isCollection(TActionFunctionReturnType x) {
        return isCollection(getType(x));
    }

    public String getFullClassNameSchema(Schema schema) {
        return getPackageSchema(schema) + "." + getSimpleClassNameSchema(schema);
    }

    public String getFullClassNameCollectionRequest(Schema schema, String name) {
        return getPackageCollectionRequest(schema) + "."
                + getSimpleClassNameCollectionRequest(schema, name);
    }

    public String getFullClassNameContainer(Schema schema, String name) {
        return getPackageContainer(schema) + "." + getSimpleClassNameContainer(name);
    }

    public String getInnerType(TActionFunctionReturnType x) {
        List<String> list = x.getType();
        if (list.size() != 1) {
            throw new IllegalArgumentException(
                    "function return type must have one and only one type but was: " + x.getType());
        }
        return getInnerType(list.get(0));
    }

    public Documentation getDocumentation() {
        return docs;
    }

    public String getPackageEntitySet(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return o.pkg() + o.packageSuffixEntitySet();
    }

    public File getClassFileEntitySet(Schema schema, String name) {
        return new File(getDirectoryEntitySet(schema), Names.toSimpleClassName(name) + ".java");
    }

    public File getDirectoryEntitySet(Schema schema) {
        SchemaOptions o = getOptions(schema);
        return toDirectory(output, o.pkg() + o.packageSuffixEntitySet());
    }

    public static String getPutMethod(String name, HttpMethod method) {
        return method.toString().toLowerCase(Locale.ENGLISH) + upperFirst(name);
    }
    
    public static String getPutChunkedMethod(String name, HttpMethod method) {
        return method.toString().toLowerCase(Locale.ENGLISH) + "Chunked" +  upperFirst(name);
    }
    
    public Property property(Structure<?> structure, String propertyName) {
        NamedStructure s = new NamedStructure(structure, propertyName);
        PropertyWithFieldName v = structureProperties.get(s);
        if (v == null) {
            // lazy load
            Set<String> fieldNames = new HashSet<>();
            structure.getHeirarchy().stream() //
                    .flatMap(x -> x.getProperties().stream()) //
                    .sorted((a, b) -> a.getName().compareTo(b.getName())) //
                    .forEach(p -> {
                        String fieldName = Names.getIdentifier(p.getName());
                        while (fieldNames.contains(fieldName)) {
                            fieldName += "_";
                        }
                        fieldNames.add(fieldName);
                        structureProperties.put(new NamedStructure(structure, p.getName()),
                                new PropertyWithFieldName(p, fieldName));
                    });
        }
        PropertyWithFieldName p = structureProperties.get(s);
        return new Property(((TProperty) p.property), p.fieldName, this);
    }
    
    private static final class PropertyWithFieldName {
        final Object property;
        final String fieldName;
        
        PropertyWithFieldName(Object property, String fieldName) {
            this.property = property;
            this.fieldName = fieldName;
        }
    }
    
    private static final class NamedStructure {

        final Structure<?> structure;
        final String propertyName;
        
        NamedStructure(Structure<?> structure, String propertyName) {
            this.structure = structure;
            this.propertyName = propertyName;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(propertyName, structure);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NamedStructure other = (NamedStructure) obj;
            return Objects.equals(propertyName, other.propertyName) && Objects.equals(structure, other.structure);
        }
    }
    
}
