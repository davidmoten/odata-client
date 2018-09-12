package com.github.davidmoten.odata.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

public final class Path {

    private final PathStyle style;
    private final StringBuilder url;
    private final List<String> queries = new ArrayList<>();

    public Path(String base, PathStyle style) {
        this.url = new StringBuilder(base);
        this.style = style;
    }

    private Path append(String s) {
        url.append(encode(s));
        return this;
    }

    public void addSegment(String segment) {
        addSegmentDelimiter();
        append(segment);
    }

    private void addSegmentDelimiter() {
        if (url.charAt(url.length() - 1) != '/') {
            url.append('/');
        }
    }

    public void addKeys(String... keys) {
        if (style == PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS) {
            if (keys.length > 0) {
                append("(");
                boolean first = true;
                for (String key : keys) {
                    Preconditions.checkNotNull(key);
                    if (!first) {
                        append(",");
                        first = false;
                    }
                    append(key);
                }
                append(")");
            }
        } else {
            if (keys.length > 0) {
                for (String key : keys) {
                    Preconditions.checkNotNull(key);
                    addSegmentDelimiter();
                    append(key);
                }
            }
        }
    }

    public void addQuery(String query) {
        queries.add(query);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(url);
        if (!queries.isEmpty()) {
            b.append("?");
            boolean first = true;
            for (String query : queries) {
                if (!first) {
                    b.append("&");
                    first = false;
                }
                b.append(encode(query));
            }
        }
        return b.toString();
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
