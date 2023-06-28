package com.adgrowth.internal.enums;

public enum AdType {
    INTERSTITIAL("INTERSTITIAL"),
    BANNER("BANNER"),
    REWARDED("REWARDED");
    private final String name;

    AdType(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }

}
