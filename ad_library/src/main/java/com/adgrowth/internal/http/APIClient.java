package com.adgrowth.internal.http;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.internal.exceptions.APIIOException;
import com.adgrowth.internal.helpers.QueryStringHelpers;

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

    private String mBaseUrl = "https://apiad-hml.adgrowth.com";

    public APIClient(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public APIClient() {
    }

    public JSONObject get(String path, HashMap<String, Object> params) throws APIIOException {

        String query = QueryStringHelpers.encode(params);
        String urlString = mBaseUrl + path + query;
        
        try {
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDefaultUseCaches(false);
            connection.setUseCaches(false);

            connection.setRequestProperty("client_key", AdServer.getClientKey());

            Response resp = new Response(connection);

            return resp.json();
        } catch (IOException e) {
            throw new APIIOException(500, "internal_error");
        }

    }


    public JSONObject post(String path, HashMap<String, String> params) throws APIIOException {

        String urlString = mBaseUrl + path;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("client_key", AdServer.getClientKey());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] requestBody = new JSONObject(params).toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(requestBody);

            Response resp = new Response(connection);

            return resp.json();
        } catch (IOException e) {
            throw new APIIOException(500, "internal_error");
        }
    }


    public class Response {
        private final HttpURLConnection connection;
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
            this.connection = connection;

            try {
                this.statusCode = connection.getResponseCode();

                this.message = connection.getResponseMessage();
                InputStream inputStream;

                Boolean successful = (statusCode / 100 == 2); // 2xx = true

                if (!successful) inputStream = connection.getErrorStream();
                else inputStream = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                this.responseString = response.toString();

                if (!successful) throw new APIIOException(this);

            } catch (IOException e) {
                throw new APIIOException(this);

            }

        }

        public JSONObject json() {
            if (responseString != null) try {
                return new JSONObject(responseString);
            } catch (JSONException ignored) {
            }
            return new JSONObject();
        }

        public String getMessage() {
            return message;
        }
    }

}

