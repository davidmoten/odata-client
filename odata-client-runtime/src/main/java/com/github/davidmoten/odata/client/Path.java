package com.github.davidmoten.odata.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Immutable!
 */
public class Path {

    private final String url;
    private final Map<String, String> queries; // unencoded
    private final PathStyle style;

    // TODO support repeated query keys?
    public Path(String url, Map<String, String> queries, PathStyle style) {
        this.url = url;
        this.queries = queries;
        this.style = style;
    }

    public Path(String url, PathStyle style) {
        this(url, Collections.emptyMap(), style);
    }

    public PathStyle style() {
        return style;
    }

    private static String append(String url, String s) {
        return url + encode(s.toString());
    }

    public Path addSegment(String segment) {
        String u = url;
        u = addSegmentDelimiter(u);
        u = append(u, segment);
        return new Path(u, queries, style);
    }

    public Path appendToSegment(String s) {
        return new Path(url + encode(s), queries, style);
    }

    private static String addSegmentDelimiter(String url) {
        if (url.charAt(url.length() - 1) != '/') {
            return url + '/';
        } else {
            return url;
        }
    }

    public Path addKeys(NameValue... keys) {
        String u = url;
        if (style == PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS) {
            if (keys.length > 0) {
                u = append(u, "(");
                boolean first = true;
                for (NameValue key : keys) {
                    Preconditions.checkNotNull(key);
                    if (!first) {
                        u = append(u, ",");
                    }
                    // as per odata 4.01 ABNF https://docs.oasis-open.org/odata/odata/v4.01/os/abnf/odata-abnf-construction-rules.txt
                    String primitiveLiteral = primitiveLiteral(key.value(), key.cls());
                    if (keys.length == 1) {
                        u = append(u, primitiveLiteral);
                    } else {
                        u = append(u, key.name().map(x -> x + "=").orElse("") + primitiveLiteral);
                    }
                    first = false;
                }
                u = append(u, ")");
            }
        } else {
            if (keys.length > 0) {
                for (NameValue key : keys) {
                    Preconditions.checkNotNull(key);
                    u = addSegmentDelimiter(u);
                    u = append(u, key.value(), key.cls());
                }
            }
        }
        return new Path(u, queries, style);
    }

    private String append(String u, Object value, Class<?> cls) {
        Preconditions.checkNotNull(value);
        // TODO don't expect null values here, explanation would be nice
        return u + encode(value.toString());
    }

    private static String primitiveLiteral(Object value, Class<?> cls) {
        if (value == null) {
            return "null";
        } else if (cls.equals(String.class)) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else {
            // By great luck all the OffsetDateTime, LocalDate, LocalTime, Duration, UUID toString methods give us
            // the format that we want (see EdmSchemaInfo.java for list of Edm schema java types)
            return value.toString();
        }
    }

    public Path addQuery(String key, String value) {
        Map<String, String> map = new LinkedHashMap<String, String>(queries);
        map.put(key, value);
        return new Path(url, map, style);
    }

    public Path clearQueries() {
        return new Path(url, Collections.emptyMap(), style);
    }

    public String toUrl() {
        StringBuilder b = new StringBuilder();
        b.append(url);
        if (!queries.isEmpty()) {
            b.append("?");
            boolean first = true;
            for (Entry<String, String> entry : queries.entrySet()) {
                if (!first) {
                    b.append("&");
                }
                first = false;
                b.append(encodeQuery(entry.getKey()));
                b.append("=");
                b.append(encodeQuery(entry.getValue()));
            }
        }
        return b.toString();
    }

    @Override
    public String toString() {
        return toUrl();
    }

    private Object encodeQuery(String query) {
        // can include = legally in query parameters
        return encode(query).replaceAll("\\%3D", "=").replaceAll("\\%24", "\\$");
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8") //
                    .replaceAll("\\+", "%20") //
                    .replaceAll("\\%21", "!") //
                    .replaceAll("\\%27", "'") //
                    .replaceAll("\\%28", "(") //
                    .replaceAll("\\%29", ")") //
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
