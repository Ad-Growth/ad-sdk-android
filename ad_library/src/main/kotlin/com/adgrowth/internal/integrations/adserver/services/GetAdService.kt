package com.adgrowth.internal.integrations.adserver.services

import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.http.HttpClient
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import java.util.HashMap

class GetAdService(private val unitId: String) {

    private val mHttpClient: HttpClient = HttpClient()

    @Throws(AdRequestException::class)
    fun run(params: HashMap<String, Any>? = HashMap()): Ad {
        if (!AdServer.isInitialized) {
            throw AdRequestException(AdRequestException.SDK_NOT_INITIALIZED)
        }
        val profile = AdServer.clientProfile


        params!!["unit_id"] = unitId

        if (profile.minAge != 0 || profile.maxAge != 0) {
            params["min_age"] = profile.minAge
            params["max_age"] = profile.maxAge
        } else if (profile.age != 0) params["age"] = profile.age

        params["gender"] = profile.gender.toString()
        params["interests"] = profile.interests

        val clientAddress = profile.clientAddress

        params["city"] = clientAddress.city
        params["state"] = clientAddress.state
        params["country"] = clientAddress.country

        return try {
            val response = mHttpClient["/adserver/api/adverts/search", params]
            Ad(response)
        } catch (e: APIIOException) {
            throw IOErrorHandler.handle(e)
        }
    }
}