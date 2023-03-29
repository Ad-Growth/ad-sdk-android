package com.adgrowth.adserver.constants;

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
