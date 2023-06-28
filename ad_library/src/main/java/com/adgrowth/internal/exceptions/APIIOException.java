package com.adgrowth.internal.exceptions;


import com.adgrowth.internal.http.APIClient;

import org.json.JSONException;
import org.json.JSONObject;


public class APIIOException extends Exception {
    private String mMessage;

    private JSONObject body;
    private int statusCode;

    public APIIOException(APIClient.Response response) {
        this.statusCode = response.getStatusCode();
        this.mMessage = response.getMessage();

        try {
            body = response.json();

            if (body != null && body.has("message"))
                mMessage = (String) body.get("message");

        } catch (JSONException ignored) {
        }
    }

    public APIIOException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.mMessage = message;
        body = new JSONObject();

    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    public JSONObject getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
