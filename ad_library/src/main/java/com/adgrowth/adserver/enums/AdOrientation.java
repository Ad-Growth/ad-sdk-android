package com.adgrowth.adserver.enums;

public enum AdOrientation {
    LANDSCAPE("LANDSCAPE"),
    PORTRAIT("PORTRAIT");
    private final String name;

    AdOrientation(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }


}
