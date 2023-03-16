package com.adgrowth.adserver.exceptions;

public class AdRequestException extends Exception {
    public final static int PLAYBACK_ERROR = 3000;
    public final static int NETWORK_ERROR = 2000;
    public final static int INTERNAL_ERROR = -5000;
    public final static int CLIENT_KEY_ERROR = -1000;
    private int code;

    public AdRequestException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
