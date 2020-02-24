package com.github.davidmoten.odata.client;

public final class RequestHeader {

    public static final RequestHeader ODATA_VERSION = create("OData-Version", "4.0");

    public static final RequestHeader ACCEPT_JSON = create("Accept", "application/json");

    private final String name;
    private final String value;

    public RequestHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static RequestHeader create(String name, String value) {
        return new RequestHeader(name, value);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public static RequestHeader acceptJsonWithMetadata(String metadata) {
        return new RequestHeader("Accept", "application/json;odata.metadata=" + metadata);
    }

    public static RequestHeader contentTypeJsonWithMetadata(String metadata) {
        return new RequestHeader("Content-Type", "application/json;odata.metadata=" + metadata);
    }

    public static RequestHeader ACCEPT_JSON_METADATA_MINIMAL = acceptJsonWithMetadata("minimal");

    public static final RequestHeader CONTENT_TYPE_JSON_METADATA_MINIMAL = contentTypeJsonWithMetadata(
            "minimal");

    public static RequestHeader ACCEPT_JSON_METADATA_FULL = acceptJsonWithMetadata("full");

    public static final RequestHeader CONTENT_TYPE_JSON_METADATA_FULL = contentTypeJsonWithMetadata(
            "full");

    public static RequestHeader ACCEPT_JSON_METADATA_NONE = acceptJsonWithMetadata("none");

    public static final RequestHeader CONTENT_TYPE_JSON_METADATA_NONE = contentTypeJsonWithMetadata(
            "none");

    public boolean isAcceptJsonWithMetadata() {
        return name.equals("Accept") && value.contains("application/json;odata.metadata=");
    }
    
    public boolean isContentTypeJsonWithMetadata() {
        return name.equals("Content-Type") && value.contains("application/json;odata.metadata=");
    }

    @Override
    public String toString() {
        return "[name=" + name + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestHeader other = (RequestHeader) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
