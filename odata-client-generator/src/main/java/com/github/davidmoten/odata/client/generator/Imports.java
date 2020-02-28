package com.github.davidmoten.odata.client.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Imports {

    Imports(String fullClassName) {
        add(fullClassName);
    }

    private final Map<String, String> map = new HashMap<>();

    public String add(Class<?> cls) {
        return add(cls.getCanonicalName().replace("$", "."));
    }

    public String add(String className) {
        final String simpleName = simpleName(className);
        String c = map.get(simpleName);
        if (c == null) {
            map.put(simpleName, className);
            return simpleName;
        } else if (c.equals(className)) {
            return simpleName;
        } else {
            return className;
        }
    }

    private static String simpleName(String className) {
        final String simpleName;
        int i = className.lastIndexOf('.');
        if (i == -1) {
            simpleName = className;
        } else {
            simpleName = className.substring(i + 1, className.length());
        }
        return simpleName;
    }

    @Override
    public String toString() {
        String x = map.values().stream().sorted() //
                .filter(c -> !c.startsWith("java.lang.")) //
                .filter(c -> !c.equals("boolean")) //
                .filter(c -> !c.equals("short")) //
                .filter(c -> !c.equals("float")) //
                .filter(c -> !c.equals("double")) //
                .filter(c -> !c.equals("int")) //
                .filter(c -> !c.equals("byte")) //
                .map(c -> "import " + c + ";").collect(Collectors.joining("\n"));
        if (!x.isEmpty()) {
            x = x + "\n\n";
        }
        return x;
    }

}