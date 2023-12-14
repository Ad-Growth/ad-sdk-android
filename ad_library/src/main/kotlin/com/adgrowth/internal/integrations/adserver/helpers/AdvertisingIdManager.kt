package com.adgrowth.internal.integrations.adserver.helpers

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient


object AdvertisingIdManager {
    @JvmStatic
    fun getAdvertisingId(context: Context): String {
        try {
            val advertisingIdInfoListenableFuture =
                AdvertisingIdClient.getAdvertisingIdInfo(context)
            return advertisingIdInfoListenableFuture.id ?: ""
        } catch (ignored: Exception) {}
        return ""
    }
}
