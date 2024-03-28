package com.adgrowth.internal.integrations.adserver.services

import android.util.Log
import com.adgrowth.adserver.BuildConfig
import com.adgrowth.internal.http.HttpClient
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.integrations.InitializationManager
import com.adgrowth.internal.integrations.adserver.AdServerInitializer
import com.adgrowth.internal.integrations.adserver.helpers.AdUriHelpers.getUniqueId
import com.adgrowth.internal.integrations.adserver.helpers.AdUriHelpers.openUrl
import com.adgrowth.internal.integrations.adserver.helpers.AdUriHelpers.replaceURISnippets
import com.adgrowth.internal.interfaces.managers.AdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService as ISendAdEventService
import java.util.*

class SendAdEventService(override val manager: AdManager<*, *>) : ISendAdEventService(manager) {

    private val mHttpClient: HttpClient = HttpClient()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun run(type: AdEventType, ad: Ad) {
        val context = manager.context
        val uniqueId = getUniqueId()

        if (type == AdEventType.CLICK) {
            try {
                openUrl(context, ad.actionUrl, manager.ipAddress, uniqueId)
            } catch (e: Exception) {
                // third-party url errors doesn't matter
            }
        }

        val timer = Timer()

        val delayedThreadStartTask = object : TimerTask() {
            override fun run() {
                val impressionTask = scope.async {
                    try {
                        val mThirdPartyHttpClient = HttpClient(
                            replaceURISnippets(ad.impressionUrl, ad.ipAddress, uniqueId)
                        )
                        mThirdPartyHttpClient[""]
                    } catch (e: APIIOException) {
                        // third-party url errors doesn't matter
                    }
                }

                val eventTask = scope.async {
                    try {
                        val params = HashMap<String, Any>()

                        params["unit_id"] = manager.unitId
                        params["type"] = type.toString()
                        params["ad_id"] = manager.adId
                        params["ip"] = manager.ipAddress
                        params["click_id"] = uniqueId
                        params["site_id"] = InitializationManager.APP_META_DATA.appId
                        params["advertising_id"] = InitializationManager.ADVERTISING_ID

                        mHttpClient["/ads/adverts/events", params]
                    } catch (e: APIIOException) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace()
                        }
                    }
                    return@async
                }

                scope.launch {
                    impressionTask.start()
                    if (!InitializationManager.APP_META_DATA.isDevKey)
                        eventTask.start()
                }
            }
        }
        timer.schedule(delayedThreadStartTask, 2000)
    }
}
