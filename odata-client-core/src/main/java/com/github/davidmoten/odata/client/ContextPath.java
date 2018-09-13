package com.github.davidmoten.odata.client;

public final class ContextPath {

    private final Path path;
    private final Context context;

    public ContextPath(Path path, Context context) {
        this.path = path;
        this.context = context;
    }

    public ContextPath addSegment(String segment) {
        return new ContextPath(path.addSegment(segment), context);
    }

    public ContextPath addKeys(String... keys) {
        return new ContextPath(path.addKeys(keys), context);
    }

    public ContextPath addQuery(String query) {
        return new ContextPath(path.addQuery(query), context);
    }

    public String toUrl() {
        return path.toUrl();
    }

    public Context context() {
        return context;
    }

}
