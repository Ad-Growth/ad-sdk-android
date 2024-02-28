package com.adgrowth.internal.integrations.admob.services

import com.adgrowth.adserver.BuildConfig
import com.adgrowth.internal.http.HttpClient
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.integrations.InitializationManager
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.integrations.admob.services.interfaces.SendAdEventService as ISendAdEventService
import java.util.*

class SendAdEventService(override val manager: AdManager<*, *>) : ISendAdEventService(manager) {

    private val mHttpClient: HttpClient = HttpClient()

    override fun run(type: AdEventType) {
        Thread {
            try {
                val params = HashMap<String, Any>()

                params["unit_id"] = manager.unitId
                params["type"] = type.toString()
                params["ad_id"] = manager.adId
                params["ip"] = manager.ipAddress
                params["click_id"] = UUID.randomUUID().toString()
                params["site_id"] = manager.context.packageName
                params["advertising_id"] = InitializationManager.ADVERTISING_ID

                mHttpClient["/ads/adverts/events", params]
            } catch (e: APIIOException) {
                // TODO: ignore?
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
