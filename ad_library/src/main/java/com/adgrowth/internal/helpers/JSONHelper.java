package com.adgrowth.internal.helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
    public static String safeGetString(JSONObject json, String field) {
        try {
            String media_url = json.getString(field);
            if (media_url != null) return media_url;
            return "";
        } catch (JSONException e) {
            return "";
        }
    }

    public static int safeGetInt(JSONObject json, String field) {
        try {
            return json.getInt(field);
        } catch (JSONException e) {
            return 0;
        }
    }

    public static double safeGetDouble(JSONObject json, String field) {
        try {
            return json.getDouble(field);
        } catch (JSONException e) {
            return 0.0;
        }
    }
}
