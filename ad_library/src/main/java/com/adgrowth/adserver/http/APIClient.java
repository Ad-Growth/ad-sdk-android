package com.adgrowth.adserver.http;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.exceptions.APIIOException;
import com.adgrowth.adserver.helpers.QueryStringHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class APIClient {
    private String baseUrl = "http://10.0.1.20:9000";
    private String client_key;

    public APIClient() {
        client_key = AdServer.getClientKey();
    }

    public JSONObject get(String path, HashMap<String, Object> params) throws APIIOException {
        String query = QueryStringHelpers.encode(params);
        String urlString = baseUrl + path + query;

        try {
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("client_key", client_key);

            Response resp = new Response(connection);

            return resp.json();
        } catch (IOException e) {
            throw new APIIOException(e);
        }

    }


    public JSONObject post(String path, HashMap<String, String> params) throws APIIOException {

        String query = QueryStringHelpers.encode(params);
        String urlString = baseUrl + path + query;

        try {

            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("client_key", client_key);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] requestBody = new JSONObject(params).toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(requestBody);

            Response resp = new Response(connection);

            return resp.json();
        } catch (IOException e) {
            throw new APIIOException(e);
        }
    }


    public class Response {
        private String responseString;
        private final String message;
        private final int statusCode;

        public String string() {
            return responseString;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public Response(HttpURLConnection connection) throws APIIOException {

            try {
                this.statusCode = connection.getResponseCode();
                this.message = connection.getResponseMessage();

                if (statusCode / 100 == 2) {

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    
                    this.responseString = response.toString();
                } else {
                    throw new APIIOException(this);
                }
            } catch (IOException e) {
                throw new APIIOException(this);
            }
        }

        public JSONObject json() {
            if (responseString != null)
                try {
                    return new JSONObject(this.responseString);
                } catch (JSONException ignored) {
                }
            return new JSONObject();
        }

        public String getMessage() {
            return message;
        }
    }

}

