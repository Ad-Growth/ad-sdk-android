package com.adgrowth.internal.http


import com.adgrowth.adserver.BuildConfig
import com.adgrowth.internal.integrations.adserver.helpers.QueryStringHelpers
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.integrations.InitializationManager

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpClient {
    private var mBaseUrl = BuildConfig.API_ENDPOINT

    constructor()
    constructor(baseUrl: String) {
        mBaseUrl = baseUrl
    }

    @Throws(APIIOException::class)
    operator fun get(path: String, params: HashMap<String, Any> = HashMap()): JSONObject {
        val query = QueryStringHelpers.encode(params)
        val urlString = mBaseUrl + path + query
        return try {
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.defaultUseCaches = false
            connection.useCaches = false

            // send only for our server
            if (mBaseUrl.startsWith(BuildConfig.API_ENDPOINT))
                connection.setRequestProperty("client_key", InitializationManager.CLIENT_KEY)

            val resp = Response(connection)
            resp.json()
        } catch (e: IOException) {
            throw APIIOException(HTTPStatusCode.INTERNAL_ERROR, "internal_error")
        }
    }

    inner class Response(connection: HttpURLConnection) {
        private var responseString: String? = null
        var message: String? = null
        var statusCode = HTTPStatusCode.NO_CONTENT
        fun string(): String? {
            return responseString
        }

        init {
            try {
                statusCode = connection.responseCode
                message = connection.responseMessage
                val inputStream: InputStream
                val successful = statusCode / 100 == 2 // 2xx = true
                inputStream = if (!successful) connection.errorStream else connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                responseString = response.toString()
                if (!successful) throw APIIOException(this)
            } catch (e: IOException) {
                throw APIIOException(this)
            }
        }

        fun json(): JSONObject {
            if (responseString != null) try {
                return JSONObject(responseString)
            } catch (ignored: JSONException) {}
            return JSONObject()
        }
    }
}
