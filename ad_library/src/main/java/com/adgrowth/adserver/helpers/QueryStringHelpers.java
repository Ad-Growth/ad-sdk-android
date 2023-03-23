package com.adgrowth.adserver.helpers;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.Iterator;
import java.util.Map;


public class QueryStringHelpers {
    public static String encode(JSONObject json) {
        StringBuilder result = new StringBuilder();
        encodeObject(result, "", json);
        return result.toString();
    }

    public static String encode(Map map) {
        StringBuilder result = new StringBuilder();
        encodeObject(result, "", new JSONObject(map));
        return "?" + result;
    }

    private static void encodeObject(StringBuilder result, String prefix, Object object) {
        try {
            if (object == null) return;
            if (object instanceof JSONArray) {
                JSONArray array = (JSONArray) object;
                for (int i = 0; i < array.length(); i++) {
                    try {
                        String newPrefix = prefix.isEmpty() ? i + "" : prefix + Uri.encode("[" + i + "]");
                        Object value = array.get(i);
                        encodeObject(result, newPrefix, value);
                        if (i + 1 < array.length() || value instanceof JSONObject || value instanceof JSONArray) {
                            result.append("&");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // throw new RuntimeException(e);
                    }
                }
            } else if (object instanceof JSONObject) {

                JSONObject json = (JSONObject) object;
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String newPrefix = prefix.isEmpty() ? key : prefix + Uri.encode("[" + key + "]");
                    try {
                        Object value = json.get(key);
                        encodeObject(result, newPrefix, value);
                        if (keys.hasNext() || value instanceof JSONObject || value instanceof JSONArray) {
                            result.append("&");
                        }
                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
                    }
                }
            } else {
                Object encodedValue = object;
                if (object instanceof String)
                    encodedValue = URLEncoder.encode(object.toString(), "UTF-8");
                result.append(prefix).append("=").append(encodedValue);

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
        }
    }
}
