package com.adgrowth.internal.integrations.adserver.http

import com.adgrowth.adserver.AdServer.clientKey
import com.adgrowth.internal.integrations.adserver.helpers.QueryStringHelpers
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpClient {
    private var mBaseUrl = "https://apiad-hml.adgrowth.com"

    constructor()
    constructor(baseUrl: String) {
        mBaseUrl = baseUrl
    }


    @Throws(APIIOException::class)
    operator fun get(path: String, params: HashMap<String, Any>): JSONObject {
        val query = QueryStringHelpers.encode(params)
        val urlString = mBaseUrl + path + query
        return try {
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.defaultUseCaches = false
            connection.useCaches = false
            connection.setRequestProperty("client_key", clientKey)
            val resp = Response(connection)
            resp.json()
        } catch (e: IOException) {
            throw APIIOException(500, "internal_error")
        }
    }

    inner class Response(connection: HttpURLConnection) {
        private var responseString: String? = null
        var message: String? = null
        var statusCode = 0
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
            } catch (ignored: JSONException) {
            }
            return JSONObject()
        }
    }
}
