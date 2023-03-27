package com.adgrowth.adserver.exceptions;

public class AdRequestException extends Exception {
    public final static String PLAYBACK_ERROR = "playback_error";
    public final static String NETWORK_ERROR = "network_error";
    public final static String INTERNAL_ERROR = "already_loaded";
    public final static String INVALID_CLIENT_KEY = "invalid_client_key";
    public final static String INVALID_UNIT_ID = "invalid_unit_id";
    public final static String NO_AD_FOUND = "no_ad_found";
    public static final String ALREADY_LOADED = "already_loaded";
    private String code;

    public AdRequestException(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
