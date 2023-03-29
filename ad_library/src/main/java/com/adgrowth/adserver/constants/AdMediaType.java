package com.adgrowth.adserver.constants;

public enum AdMediaType {
    IMAGE("IMAGE"), VIDEO("VIDEO"), EMBEDDED("EMBEDDED");
    private final String name;

    AdMediaType(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }

}
