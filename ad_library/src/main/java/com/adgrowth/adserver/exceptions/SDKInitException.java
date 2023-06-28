package com.adgrowth.adserver.exceptions;

public class SDKInitException extends Exception {

    public static final String UNAUTHORIZED_CLIENT_KEY = "unauthorized_client_key";
    public static final String ALREADY_INITIALIZED = "already_initialized";
    public static final String INTERNAL_ERROR = "internal_error";
    public static final String NETWORK_ERROR = "network_error";
    public static final String UNKNOWN_ERROR = "unknown_error";
    private String mCode;
    private String message;

    public SDKInitException(AdRequestException e) {
        handleError(e.getCode());
    }

    public SDKInitException(String errorCode) {
        mCode = errorCode;
        setMessage(mCode);
    }

    private void handleError(String errorCode) {
        switch (errorCode) {
            case AdRequestException.NETWORK_ERROR:
                mCode = NETWORK_ERROR;
                break;
            case AdRequestException.INTERNAL_ERROR:
                mCode = INTERNAL_ERROR;
                break;
            case AdRequestException.UNKNOWN_ERROR:
            default:
                mCode = UNKNOWN_ERROR;
                break;
        }

        setMessage(mCode);
    }

    private void setMessage(String code) {
        switch (code) {

            case ALREADY_INITIALIZED:
                message = "You already initialized this with this key, just use it.";
                break;
            case UNAUTHORIZED_CLIENT_KEY:
                message = "The provided client key is not valid.";
                break;
            case UNKNOWN_ERROR:
            case INTERNAL_ERROR:
                message = "Sorry, this was an unexpected error, please try again.";
                break;
            default:
        }
    }

    public String getCode() {
        return mCode;
    }

    public String getMessage() {
        return message;
    }
}
