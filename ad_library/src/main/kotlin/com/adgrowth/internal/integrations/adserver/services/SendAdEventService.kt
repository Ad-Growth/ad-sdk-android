package com.adgrowth.internal.integrations.adserver.services

import android.app.Activity
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.BuildConfig
import com.adgrowth.internal.integrations.adserver.http.HttpClient
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.enums.AdEventType
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException
import java.util.*

class SendAdEventService(private val unitId: String) {

    private val mHttpClient: HttpClient = HttpClient()


    fun run(context: Activity, type: AdEventType, ad: Ad) {
        try {
            val params = HashMap<String, Any>()
            params["unit_id"] = unitId
            params["type"] = type.toString()
            params["ad_id"] = ad.id
            params["ip"] = ad.ipAddress
            params["click_id"] = UUID.randomUUID().toString()
            params["site_id"] = context.packageName
            params["advertising_id"] = AdServer.adId

            mHttpClient["/adserver/api/adverts/events", params]
        } catch (ignored: APIIOException) {
            // TODO: ignore?
            if (BuildConfig.DEBUG) {
                ignored.printStackTrace()
            }
        }
    }
}
