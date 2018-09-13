package com.github.davidmoten.odata.client;

public final class PathWithContext {

    private final Path path;
    private final Context context;

    public PathWithContext(Path path, Context context) {
        this.path = path;
        this.context = context;
    }

    public PathWithContext addSegment(String segment) {
        return new PathWithContext(path.addSegment(segment), context);
    }

    public PathWithContext addKeys(String... keys) {
        return new PathWithContext(path.addKeys(keys), context);
    }

    public PathWithContext addQuery(String query) {
        return new PathWithContext(path.addQuery(query), context);
    }

    public String toUrl() {
        return path.toUrl();
    }

    public Context context() {
        return context;
    }

}
