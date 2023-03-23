package com.adgrowth.adserver.exceptions;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class APIIOException extends IOException {
    private String message;


    private JSONObject body;
    private int statusCode;

    public APIIOException(Response response) {
        this.statusCode = response.code();
        this.message = response.message();

        try {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                this.body = new JSONObject(responseBody.string());
                this.message = (String) this.body.get("message");
            }
        } catch (JSONException | IOException ignored) {
        }
    }

    public APIIOException(Exception e) {
        e.printStackTrace();
        this.statusCode = 500;
        this.body = new JSONObject();
        this.message = "Unexpected error";

    }

    @Override
    public String getMessage() {
        return message;
    }

    public JSONObject getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
