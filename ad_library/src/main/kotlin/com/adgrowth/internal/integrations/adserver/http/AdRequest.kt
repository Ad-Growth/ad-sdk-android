package com.adgrowth.internal.integrations.adserver.http

import android.app.Activity
import com.adgrowth.adserver.BuildConfig
import com.adgrowth.internal.integrations.adserver.helpers.AdUriHelpers.openUrl
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.enums.AdEventType
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import com.adgrowth.internal.integrations.adserver.services.GetAdService
import com.adgrowth.internal.integrations.adserver.services.SendAdEventService

class AdRequest(val unitId: String) {
    private val getAdService = GetAdService(unitId)
    private val sendAdEventService = SendAdEventService(unitId)

    fun getAd(options: HashMap<String, Any>?): Ad {

        return try {
            getAdService.run(options)
        } catch (e: APIIOException) {
            throw IOErrorHandler.handle(e)
        }
    }

    fun sendImpression(context: Activity, ad: Ad) {
        Thread {
            try {
                sendAdEventService.run(context, AdEventType.VIEW, ad)
            } catch (ignored: APIIOException) {
                // TODO: ignore?
                if (BuildConfig.DEBUG) {
                    ignored.printStackTrace()
                }
            }
        }.start()
    }

    fun sendClick(context: Activity, ad: Ad) {
        Thread {
            sendAdEventService.run(context, AdEventType.CLICK, ad)
        }.start()
        openUrl(context, ad.actionUrl, ad.ipAddress)
    }

}