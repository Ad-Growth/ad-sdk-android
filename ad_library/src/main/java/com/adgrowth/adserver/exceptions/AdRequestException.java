package com.adgrowth.adserver.exceptions;

public class AdRequestException extends Exception {
    public final static String PLAYBACK_ERROR = "playback_error";
    public final static String NETWORK_ERROR = "network_error";
    public final static String UNKNOWN_ERROR = "unknown_error";
    public final static String UNIT_ID_MISMATCHED_AD_TYPE = "unit_id_mismatched_ad_type";
    public final static String INTERNAL_ERROR = "internal_error";
    public final static String INVALID_CLIENT_KEY = "invalid_client_key";
    public final static String INVALID_UNIT_ID = "invalid_unit_id";
    public final static String SDK_NOT_INITIALIZED = "sdk_not_initialized";
    public final static String NO_AD_FOUND = "no_ad_found";
    public static final String ALREADY_LOADED = "already_loaded";
    private String mCode;

    public AdRequestException(String code) {
        this.mCode = code;
    }

    public String getCode() {
        return mCode;
    }

}
