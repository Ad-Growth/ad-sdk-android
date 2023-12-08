package com.adgrowth.adserver.enums;

public enum AdSize {
    BANNER("BANNER"),
    LARGE_BANNER("LARGE_BANNER"),
    MEDIUM_RECTANGLE("MEDIUM_RECTANGLE"),
    FULL_BANNER("FULL_BANNER"),
    LEADERBOARD("LEADERBOARD");
    private final String name;

    AdSize(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }


}
