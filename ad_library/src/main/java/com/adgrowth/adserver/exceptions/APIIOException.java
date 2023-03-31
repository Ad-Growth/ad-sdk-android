package com.adgrowth.adserver.exceptions;


import com.adgrowth.internal.http.APIClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class APIIOException extends IOException {
    private String message = "";


    private JSONObject body;
    private int statusCode;

    public APIIOException(APIClient.Response response) {
        this.statusCode = response.getStatusCode();
        this.message = response.getMessage();

        try {
            body = response.json();

            if (body != null && body.has("message")) 
                message = (String) body.get("message");
            
        } catch (JSONException ignored) {}
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
