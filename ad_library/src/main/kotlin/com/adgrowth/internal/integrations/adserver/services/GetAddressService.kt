package com.adgrowth.internal.integrations.adserver.services

import com.adgrowth.adserver.entities.ClientAddress
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.http.HttpClient
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import org.json.JSONException
import org.json.JSONObject


class GetAddressService {
    private val httpClient: HttpClient = HttpClient()

    @Throws(AdRequestException::class)
    fun run(latitude: Double?, longitude: Double?): ClientAddress {
        val params = HashMap<String, Any>()


        if (latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0) {
            params["latitude"] = latitude
            params["longitude"] = longitude
        } else {
            params["latitude"] = ""
            params["longitude"] = ""
        }

        return try {
            val response = httpClient["/functions/geolocate", params]

            ClientAddress(response.getJSONObject("localization"))

        } catch (e: APIIOException) {
            // fallback
            if (e.message!!.contains("Invalid latitude/longitude")) return ClientAddress(JSONObject())
            throw IOErrorHandler.handle(e)
        } catch (e: JSONException) {
            if (e.message!!.contains("Invalid latitude/longitude")) return ClientAddress(JSONObject())
            throw IOErrorHandler.handle(e)
        }
    }
}
