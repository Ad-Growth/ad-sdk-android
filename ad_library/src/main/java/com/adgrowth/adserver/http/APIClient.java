package com.adgrowth.adserver.http;

import android.util.Log;

import com.adgrowth.adserver.AdServer;

import com.adgrowth.adserver.exceptions.APIIOException;
import com.adgrowth.adserver.helpers.QueryStringHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class APIClient {
    private final String TAG = "ADSERVER";
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private String baseUrl;

    public APIClient() {
//        init("https://api-hml.adserver.adgrowth.com");
//        init("http://10.0.1.20:8000");
        init("http://10.0.1.20:9000");
    }

    public APIClient(String baseUrl) {
        init(baseUrl);
    }

    void init(String baseUrl) {
        this.baseUrl = baseUrl;

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        this.client = clientBuilder.addInterceptor(chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Content-Type", "Application/JSON")
                    .addHeader("client_key", AdServer.getClientKey()).removeHeader("charset")
                    .build();
            Response response = chain.proceed(request);

            Log.d(TAG, "Response code: " + response.code());
            Log.d(TAG, "Response: " + response);
            assert response.body() != null;
            // throw if different of 2xx
            if (response.code() / 100 != 2) {
                throw new APIIOException(response);
            }
            return response;
        }).build();
    }

    public JSONObject get(String path) throws APIIOException {
        return runGet(path, new HashMap(), new HashMap());
    }

    public JSONObject get(String path, HashMap<String, Object> params) throws APIIOException {
        return runGet(path, params, new HashMap());
    }

    public JSONObject get(String path, HashMap<String, Object> params, HashMap<String, String> headers) throws APIIOException {
        return runGet(path, params, headers);
    }

    private JSONObject runGet(String path, HashMap<String, Object> params, HashMap<String, String> headers) throws APIIOException {

        Headers headersBuild = Headers.of(headers);
        String query = QueryStringHelpers.encode(params);
        String url = baseUrl + path + query;

        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

        Request request = new Request.Builder().url(httpBuilder.build()).headers(headersBuild).build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONObject(response.body().string());
        } catch (JSONException | IOException e) {
            if(e instanceof APIIOException) throw (APIIOException) e;

            throw new APIIOException(e);
        }

    }

    public JSONObject post(String path) throws IOException, JSONException {
        return runPost(path, new HashMap(), new HashMap());
    }

    public JSONObject post(String path, HashMap<String, String> params) throws APIIOException {
        return runPost(path, params, new HashMap());
    }

    public JSONObject post(String path, HashMap<String, String> params, HashMap<String, String> headers) throws APIIOException {
        return runPost(path, params, headers);
    }

    private JSONObject runPost(String path, HashMap<String, String> params, HashMap<String, String> headers) throws APIIOException {

        HttpUrl.Builder httpBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        RequestBody body = FormBody.create(new JSONObject(params).toString(), JSON);
        Headers headersBuild = Headers.of(headers);


        Request request = new Request.Builder().url(httpBuilder.build()).post(body).headers(headersBuild).build();

        try (Response response = client.newCall(request).execute()) {

            return new JSONObject(response.body().string());
        } catch (JSONException | IOException e) {
            if(e instanceof APIIOException) throw (APIIOException) e;
            throw new APIIOException(e);
        }

    }


}

