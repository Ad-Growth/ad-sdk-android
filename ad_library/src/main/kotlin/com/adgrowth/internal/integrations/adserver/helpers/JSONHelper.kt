package com.adgrowth.internal.integrations.adserver.helpers

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JSONHelper {
    private fun getString(json: JSONObject, field: String, defaultValue: String?): String? {
        return try {
            val mediaUrl = json.getString(field)
            mediaUrl ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }

    fun safeGetString(json: JSONObject, field: String, defaultValue: String?): String? {
        return getString(json, field, defaultValue)
    }

    fun safeGetString(json: JSONObject, field: String): String {
        return getString(json, field, "")!!
    }

    fun safeGetObject(json: JSONObject, field: String): JSONObject {
        return getObject(json, field, JSONObject())
    }

    fun safeGetBoolean(json: JSONObject, field: String, defaultValue: Boolean): Boolean {
        return getBoolean(json, field, defaultValue)
    }

    fun safeGetObject(json: JSONObject, field: String, defaultValue: JSONObject): JSONObject {
        return getObject(json, field, defaultValue)
    }

    fun safeGetArray(json: JSONObject, field: String): JSONArray {
        return getArray(json, field, JSONArray())
    }

    fun safeGetArray(json: JSONObject, field: String, defaultValue: JSONArray): JSONArray {
        return getArray(json, field, defaultValue)
    }

    private fun getObject(json: JSONObject, field: String, defaultValue: JSONObject): JSONObject {
        return try {
            json.getJSONObject(field)
        } catch (e: JSONException) {
            defaultValue
        }
    }

    private fun getBoolean(json: JSONObject, field: String, defaultValue: Boolean): Boolean {
        return try {
            json.getBoolean(field)
        } catch (e: JSONException) {
            defaultValue
        }
    }

    private fun getArray(json: JSONObject, field: String, defaultValue: JSONArray): JSONArray {
        return try {
            json.getJSONArray(field)
        } catch (e: JSONException) {
            defaultValue
        }
    }

    private fun getInt(json: JSONObject, field: String?, defaultValue: Int?): Int? {
        return try {
            json.getInt(field)
        } catch (e: JSONException) {
            defaultValue
        }
    }

    fun safeGetInt(json: JSONObject, field: String?, defaultValue: Int?): Int? {
        return getInt(json, field, defaultValue)
    }

    fun safeGetInt(json: JSONObject, field: String?): Int? {
        return getInt(json, field, 0)
    }

    private fun getDouble(json: JSONObject, field: String?, defaultValue: Double): Double {
        return try {
            json.getDouble(field)
        } catch (e: JSONException) {
            defaultValue
        }
    }

    fun safeGetDouble(json: JSONObject, field: String?, defaultValue: Double): Double {
        return getDouble(json, field, defaultValue)
    }

    fun safeGetDouble(json: JSONObject, field: String?): Double {
        return getDouble(json, field, 0.0)
    }
}
