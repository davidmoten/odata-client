package com.github.davidmoten.odata.client;

import java.util.Map;
import java.util.Map.Entry;

public final class ContextPath {

    private final Path path;
    private final Context context;

    public ContextPath(Context context, Path path) {
        this.path = path;
        this.context = context;
    }

    public ContextPath addSegment(String segment) {
        return new ContextPath(context, path.addSegment(segment));
    }
    
    public ContextPath addActionOrFunctionSegment(String fullyQualifiedName) {
        boolean useSimpleNameOnly = "true".equalsIgnoreCase(String.valueOf(context.getProperty(Properties.ACTION_OR_FUNCTION_SEGMENT_SIMPLE_NAME)));
        if (useSimpleNameOnly) {
            int i = fullyQualifiedName.lastIndexOf(".");
            if (i == -1) {
                return addSegment(fullyQualifiedName);
            } else {
                return addSegment(fullyQualifiedName.substring(i + 1));
            }
        } else {
            return addSegment(fullyQualifiedName);
        }
    }

    public ContextPath addKeys(NameValue... keys) {
        return new ContextPath(context, path.addKeys(keys));
    }

    public ContextPath addQuery(String key, String value) {
        return new ContextPath(context, path.addQuery(key, value));
    }

    public String toUrl() {
        return path.toUrl();
    }

    public Context context() {
        return context;
    }

    public Path path() {
        return path;
    }

    public ContextPath addQueries(Map<String, String> queries) {
        Path p = path;
        for (Entry<String, String> entry : queries.entrySet()) {
            p = p.addQuery(entry.getKey(), entry.getValue());
        }
        return new ContextPath(context, p);
    }

    public ContextPath clearQueries() {
        Path p = path.clearQueries();
        return new ContextPath(context, p);
    }
}
