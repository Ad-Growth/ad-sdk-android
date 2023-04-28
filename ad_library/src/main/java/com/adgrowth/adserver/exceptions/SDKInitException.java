package com.adgrowth.adserver.exceptions;

public class SDKInitException extends Exception {

    public static final String UNAUTHORIZED_CLIENT_KEY = "unauthorized_client_key";
    public static final String ALREADY_INITIALIZED = "already_initialized";
    private String code;
    private String message;

    public SDKInitException(AdRequestException e) {
        // TODO: handle and deliver a error code

        code = UNAUTHORIZED_CLIENT_KEY;


        setMessage(code);

    }

    public SDKInitException(String code) {
        // TODO: handle and deliver a error code
        setMessage(code);
    }

    private void setMessage(String code) {
        switch (code) {

            case ALREADY_INITIALIZED:
                message = "You already initialized this with this key, just use it.";
                break;
            case UNAUTHORIZED_CLIENT_KEY:
            default:
                message = "The provided client key is not valid.";
        }
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
