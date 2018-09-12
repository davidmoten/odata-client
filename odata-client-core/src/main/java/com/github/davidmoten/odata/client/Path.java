package com.github.davidmoten.odata.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Immutable!
 */
public final class Path {

    private final String url;
    private final List<String> queries; // unencoded
    private final PathStyle style;

    public Path(String url, List<String> queries, PathStyle style) {
        this.url = url;
        this.queries = queries;
        this.style = style;
    }

    private String append(String url, String s) {
        return url + encode(s);
    }

    public Path addSegment(String segment) {
        String u = url;
        u = addSegmentDelimiter(u);
        u = append(u, segment);
        return new Path(u, queries, style);
    }

    private static String addSegmentDelimiter(String url) {
        if (url.charAt(url.length() - 1) != '/') {
            return url + '/';
        } else {
            return url;
        }
    }

    public Path addKeys(String... keys) {
        String u = url;
        if (style == PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS) {
            if (keys.length > 0) {
                u = append(u, "(");
                boolean first = true;
                for (String key : keys) {
                    Preconditions.checkNotNull(key);
                    if (!first) {
                        u = append(u, ",");
                        first = false;
                    }
                    u = append(u, key);
                }
                u = append(u, ")");
            }
        } else {
            if (keys.length > 0) {
                for (String key : keys) {
                    Preconditions.checkNotNull(key);
                    u = addSegmentDelimiter(u);
                    u = append(u, key);
                }
            }
        }
        return new Path(u, queries, style);
    }

    public Path addQuery(String query) {
        List<String> list = new ArrayList<>(queries);
        list.add(query);
        return new Path(url, list, style);
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
