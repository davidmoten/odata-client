package com.github.davidmoten.odata.client;

public final class PathItem {

    private final String item;
    private final PathItemType type;

    public PathItem(String item, PathItemType type) {
        this.item = item;
        this.type = type;
    }

    public String item() {
        return item;
    }

    public PathItemType type() {
        return type;
    }

    public enum PathItemType {
        ITEM, KEY, PARAMETER;
    }
}
