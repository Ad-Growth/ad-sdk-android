package com.adgrowth.internal.exceptions

import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.http.HttpClient
import org.json.JSONException
import org.json.JSONObject

class APIIOException : Exception {
    override var message: String?
        private set
    var body: JSONObject = JSONObject()
        private set
    var statusCode: Int
        private set

    constructor(response: HttpClient.Response) {
        statusCode = response.statusCode
        message = response.message

        try {
            body = response.json()
            if (body.has("message")) message = body["message"] as String
        } catch (ignored: JSONException) {
        }
    }

    constructor(statusCode: Int, message: String) {
        this.statusCode = statusCode
        this.message = message
        body = JSONObject()
    }
}
