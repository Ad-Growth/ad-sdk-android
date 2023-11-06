package com.adgrowth.internal.integrations.adserver.helpers

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient


object AdvertisingIdManager {
    @JvmStatic
    fun getAdvertisingId(context: Context): String {
        val advertisingIdInfoListenableFuture = AdvertisingIdClient.getAdvertisingIdInfo(context)
        return advertisingIdInfoListenableFuture.id ?: ""
    }
}
