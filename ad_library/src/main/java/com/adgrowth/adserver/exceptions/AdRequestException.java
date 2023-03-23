package com.adgrowth.adserver.exceptions;

public class AdRequestException extends Exception {
    public final static int PLAYBACK_ERROR = 300;
    public final static int NETWORK_ERROR = 200;
    public final static int INTERNAL_ERROR = -5000;
    public final static int INVALID_CLIENT_KEY = -100;
    public final static int INVALID_UNIT_ID = -200;
    public final static int NO_AD_FOUND = -300;
    public static final int ALREADY_LOADED = -401;
    private int code;

    public AdRequestException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
