package com.adgrowth.internal.integrations.adserver.services

import com.adgrowth.adserver.AdServer
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HttpClient
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAdService as IGetAdServerAdService

class GetAdService(override val manager: AdManager<*, *>) : IGetAdServerAdService(manager) {

    private val mHttpClient: HttpClient = HttpClient()

    @Throws(APIIOException::class)
    override fun run(params: HashMap<String, Any>): Ad {

        val profile = AdServer.clientProfile

        params["unit_id"] = manager.unitId

        if (profile.minAge != 0 || profile.maxAge != 0) {
            params["min_age"] = profile.minAge
            params["max_age"] = profile.maxAge
        } else if (profile.age != 0) params["age"] = profile.age

        params["gender"] = profile.gender.toString()
        params["interests"] = profile.interests

        val clientAddress = profile.clientAddress

        params["city"] = clientAddress.city ?: ""
        params["state"] = clientAddress.state ?: ""
        params["country"] = clientAddress.country ?: ""

        val response = mHttpClient["/adserver/api/adverts/search", params]

        if (params.containsKey("orientation")) {
            response.getJSONObject("advert").put("orientation", params["orientation"])
        }

        val ad = Ad(response)

        manager.adId = ad.id
        manager.ipAddress = ad.ipAddress
        manager.refreshRate = ad.refreshRate
        manager.reward = ad.reward

        return ad

    }
}
