package com.adgrowth.adserver.exceptions;

public class SDKInitException extends Exception {

    public static final int UNAUTHORIZED_CLIENT_KEY = 1000;
    public static final int ALREADY_INITIALIZED = 2000;
    private int code;
    private String message;

    public SDKInitException(AdRequestException e) {
        // TODO: handle and deliver a error code

        code = UNAUTHORIZED_CLIENT_KEY;


        setMessage(code);

    }

    public SDKInitException(int code) {
        // TODO: handle and deliver a error code
        setMessage(code);
    }

    private void setMessage(int code) {
        switch (code) {

            case ALREADY_INITIALIZED:
                message = "You already initialized this with this key, just use it.";
                break;
            case UNAUTHORIZED_CLIENT_KEY:
            default:
                message = "The provided client key is not valid.";
        }
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
