package com.adgrowth.adserver.enums;

public enum AdSizeType {
    BANNER("BANNER"),
    LARGE_BANNER("LARGE_BANNER"),
    MEDIUM_RECTANGLE("MEDIUM_RECTANGLE"),
    FULL_BANNER("FULL_BANNER"),
    LEADERBOARD("LEADERBOARD");
    private final String name;

    AdSizeType(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }


}
