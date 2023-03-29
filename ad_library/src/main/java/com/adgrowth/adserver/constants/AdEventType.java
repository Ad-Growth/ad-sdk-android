package com.adgrowth.adserver.constants;

public enum AdEventType {
    PRINTED("PRINTED"),
    CLICKED("CLICKED"),
    LOADED("LOADED"),
    DISMISSED("DISMISSED"),
    REWARDED("REWARDED");
    private final String name;

    AdEventType(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }

}
