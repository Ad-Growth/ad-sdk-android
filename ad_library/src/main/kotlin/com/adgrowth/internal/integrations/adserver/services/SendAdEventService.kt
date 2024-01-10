package com.adgrowth.internal.integrations.adserver.services

import com.adgrowth.adserver.BuildConfig
import com.adgrowth.internal.http.HttpClient
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.integrations.InitializationManager
import com.adgrowth.internal.integrations.adserver.helpers.AdUriHelpers.openUrl
import com.adgrowth.internal.integrations.adserver.helpers.AdUriHelpers.replaceAdCallbackParams
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService as ISendAdEventService
import java.util.*

class SendAdEventService(override val manager: AdManager<*, *>) : ISendAdEventService(manager) {

    private val mHttpClient: HttpClient = HttpClient()

    override fun run(type: AdEventType, ad: Ad) {
        val context = manager.context
        Thread {
            when (type) {
                AdEventType.VIEW -> {
                    try {
                        val mThirdPartyHttpClient = HttpClient(
                            replaceAdCallbackParams(
                                context, ad.impressionUrl, ad.ipAddress
                            )
                        )
                        mThirdPartyHttpClient[""]
                    } catch (_: APIIOException) {
                        // third-party url errors doesn't matter
                    }

                }
                AdEventType.CLICK -> {
                    try {
                        openUrl(context, ad.actionUrl, manager.ipAddress)
                    } catch (_: Exception) {
                        // third-party url errors doesn't matter
                    }
                }
            }
        }.start()

        Thread {
            try {
                val params = HashMap<String, Any>()

                params["unit_id"] = manager.unitId

                params["type"] = type.toString()
                params["ad_id"] = manager.adId
                params["ip"] = manager.ipAddress
                params["click_id"] = UUID.randomUUID().toString()
                params["site_id"] = context.packageName
                params["advertising_id"] = InitializationManager.ADVERTISING_ID

                mHttpClient["/adserver/api/adverts/events", params]
            } catch (e: APIIOException) {
                // TODO: ignore?
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
            }
        }.start()

    }
}
