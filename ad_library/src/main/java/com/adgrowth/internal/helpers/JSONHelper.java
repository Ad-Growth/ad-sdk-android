package com.adgrowth.internal.helpers;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
    public static String getString(JSONObject json, String field, String defaultValue) {
        try {
            String media_url = json.getString(field);
            if (media_url != null) return media_url;
            return defaultValue;
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static String safeGetString(JSONObject json, String field, String defaultValue) {
        return getString(json, field, defaultValue);
    }

    public static String safeGetString(JSONObject json, String field) {
        return getString(json, field, "");
    }

    public static JSONObject safeGetObject(JSONObject json, String field) {
        return getObject(json, field, new JSONObject());
    }

    public static JSONObject safeGetObject(JSONObject json, String field, JSONObject defaultValue) {
        return getObject(json, field, defaultValue);
    }

    public static JSONObject getObject(JSONObject json, String field, JSONObject defaultValue) {
        try {
            return json.getJSONObject(field);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static Integer getInt(JSONObject json, String field, Integer defaultValue) {
        try {
            return json.getInt(field);
        } catch (JSONException e) {
            return defaultValue;
        }
    }


    public static Integer safeGetInt(JSONObject json, String field, Integer defaultValue) {
        return getInt(json, field, defaultValue);

    }

    public static Integer safeGetInt(JSONObject json, String field) {
        return getInt(json, field, 0);
    }

    public static double getDouble(JSONObject json, String field, double defaultValue) {
        try {
            return json.getDouble(field);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static double safeGetDouble(JSONObject json, String field, double defaultValue) {
        return getDouble(json, field, defaultValue);
    }

    public static double safeGetDouble(JSONObject json, String field) {
        return getDouble(json, field, 0.0);
    }


}
