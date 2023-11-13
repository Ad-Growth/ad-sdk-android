package com.adgrowth.internal.enums;

public enum AdEventType {
    VIEW("VIEW"), CLICK("CLICK");

    private final String name;

    AdEventType(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }

}
