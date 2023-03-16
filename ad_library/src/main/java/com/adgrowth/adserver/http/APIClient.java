package com.adgrowth.adserver.http;

import android.util.Log;

import androidx.annotation.Nullable;

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

class APIClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();
    //    private static final String BASE_URL = "https://api-hml.adserver.adgrowth.com";
    private static final String BASE_URL = "http://10.0.1.20:8000";


    public static JSONObject get(String path, HashMap<String, String> params, @Nullable HashMap<String, String> headers) throws JSONException, IOException {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(BASE_URL).newBuilder();

        httpBuilder.addPathSegment(path);

        Headers headersBuild = Headers.of(headers);

        if (params != null) {
            for (HashMap.Entry<String, String> param : params.entrySet()) {
                Log.d("TAG", "getKey: "+param.getKey()+", value "+ String.valueOf(param.getValue()));
                httpBuilder.addQueryParameter(param.getKey(), String.valueOf(param.getValue()));
            }
        }
        Log.d("TAG", "get: HTTP_URL "+ httpBuilder.build().toString());
        Request request = new Request.Builder().url(httpBuilder.build()).headers(headersBuild).build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONObject(response.body().string());
        }

    }

    public static JSONObject post(String path, HashMap<String, String> params, @Nullable HashMap<String, String> headers) throws IOException, JSONException {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(BASE_URL).newBuilder();

        httpBuilder.encodedPath(path);

        RequestBody body = FormBody.create(JSON, new JSONObject(params).toString());

        Headers headersBuild = Headers.of(headers);


        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .post(body)
                .headers(headersBuild)
                .build();

        try (Response response = client.newCall(request).execute()) {

            return new JSONObject(response.body().string());
        }
    }


}
