package com.adgrowth.internal.integrations.adserver.helpers

import android.net.Uri
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/** This class does something like query-string-parser lib in nodejs does */
object QueryStringHelpers {
    fun encode(map: Map<*, *>?): String {
        val result = StringBuilder()
        encodeObject(result, "", map?.let { JSONObject(it) })
        return "?$result"
    }

    private fun encodeObject(result: StringBuilder, prefix: String, `object`: Any?) {
        try {
            if (`object` == null) return
            when (`object`) {
                is JSONArray -> {
                    for (i in 0 until `object`.length()) {
                        try {
                            val newPrefix =
                                if (prefix.isEmpty()) i.toString() + "" else prefix + Uri.encode(
                                    "[$i]"
                                )
                            val value = `object`[i]
                            encodeObject(result, newPrefix, value)
                            if (i + 1 < `object`.length() || value is JSONObject || value is JSONArray) {
                                result.append("&")
                            }
                        } catch (e: JSONException) {
                            // ignore
                        }
                    }
                }
                is JSONObject -> {
                    val keys = `object`.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val newPrefix = if (prefix.isEmpty()) key else prefix + Uri.encode(
                            "[$key]"
                        )
                        try {
                            val value = `object`[key]
                            encodeObject(result, newPrefix, value)
                            if (keys.hasNext() || value is JSONObject || value is JSONArray) {
                                result.append("&")
                            }
                        } catch (e: JSONException) {
                            // ignore
                        }
                    }
                }
                else -> {
                    var encodedValue = `object`
                    if (`object` is String) encodedValue =
                        URLEncoder.encode(`object`.toString(), "UTF-8")
                    result.append(prefix).append("=").append(encodedValue)
                }
            }
        } catch (e: UnsupportedEncodingException) {
            // ignore
        }
    }
}
