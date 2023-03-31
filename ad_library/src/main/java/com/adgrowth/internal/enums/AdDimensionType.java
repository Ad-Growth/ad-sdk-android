package com.adgrowth.internal.enums;

public enum AdDimensionType {
    BANNER("BANNER"),
    LARGE_BANNER("LARGE_BANNER"),
    MEDIUM_RECTANGLE("MEDIUM_RECTANGLE"),
    FULL_BANNER("FULL_BANNER"),
    LEADERBOARD("LEADERBOARD");
    private final String name;

    AdDimensionType(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }


}
