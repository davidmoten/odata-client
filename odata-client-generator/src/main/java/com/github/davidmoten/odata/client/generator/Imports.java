package com.github.davidmoten.odata.client.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Imports {

    private final String fullClassName;

    Imports(String fullClassName) {
        this.fullClassName = fullClassName;
        add(fullClassName);
    }

    // simpleName (may be full class name if already present) to fullClassName
    private final Map<String, String> map = new HashMap<>();

    public String add(Class<?> cls) {
        return add(cls.getCanonicalName().replace("$", "."));
    }

    public String add(String className) {
        final String simplifiedName = simplifiedName(className);
        String c = map.get(simplifiedName);
        if (c == null) {
            map.put(simplifiedName, className);
            return simplifiedName;
        } else if (c.equals(className)) {
            return simplifiedName;
        } else {
            return className;
        }
    }

    private static String simplifiedName(String className) {
        final String simpleName;
        int i = className.lastIndexOf('.');
        if (i == -1) {
            simpleName = className;
        } else {
            simpleName = className.substring(i + 1);
        }
        return simpleName;
    }

    @Override
    public String toString() {
        String pkgFullClassName = pkg(fullClassName);
        String x = map //
                .values() //
                .stream() //
                .sorted() //
//                .filter(c -> !c.startsWith("java.lang.")) //
                .filter(c -> !c.equals("boolean")) //
                .filter(c -> !c.equals("short")) //
                .filter(c -> !c.equals("float")) //
                .filter(c -> !c.equals("double")) //
                .filter(c -> !c.equals("int")) //
                .filter(c -> !c.equals("byte")) //
                .filter(c -> !c.equals("long")) //
                .filter(c -> !c.equals(fullClassName))
                // ensure that if in same pkg as fullClassName that we don't need
                // to specify an import 
                .filter(c -> !pkg(c).equals(pkgFullClassName)) //
                .map(new Function<String,String>() {

                    String previous;
                    
                    @Override
                    public String apply(String c) {
                        String firstSegment = firstSegment(c);
                        boolean insertBlankLine  = previous != null && !firstSegment.equals(previous);
                        previous = firstSegment;
                        return (insertBlankLine? "\n" : "") + "import " + c + ";";
                    }
                }) //
                .collect(Collectors.joining("\n"));
        if (!x.isEmpty()) {
            x = x + "\n\n";
        }
        return x;
    }
    
    private static String pkg(String fullClassName) {
        int i = fullClassName.lastIndexOf('.');
        if (i == -1) {
            return "";
        } else {
            return fullClassName.substring(0, i);
        }
    }
    
    private static String firstSegment(String s) {
        int i = s.indexOf('.');
        if (i == -1) {
            return s;
        } else {
            return s.substring(0, i);
        }
    }

}
