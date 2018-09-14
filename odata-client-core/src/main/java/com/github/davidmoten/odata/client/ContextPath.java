package com.github.davidmoten.odata.client;

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

    public ContextPath addKeys(String... keys) {
        return new ContextPath(context, path.addKeys(keys));
    }

    public ContextPath addQuery(String query) {
        return new ContextPath(context, path.addQuery(query));
    }

    public String toUrl() {
        return path.toUrl();
    }

    public Context context() {
        return context;
    }

}
