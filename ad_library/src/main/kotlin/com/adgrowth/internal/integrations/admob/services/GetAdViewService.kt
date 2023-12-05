package com.adgrowth.internal.integrations.admob.services

import com.adgrowth.adserver.enums.AdSize as AdServerAdSize
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.helpers.LayoutHelper.Companion.getAdLayoutParams
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.AdViewManager
import com.adgrowth.internal.integrations.InitializationManager
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.google.android.gms.ads.*
import java.util.concurrent.CompletableFuture
import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService


class GetAdViewService(override val manager: AdViewManager) :
    IGetAdService<AdView>(manager) {

    override fun run(adRequest: AdRequest): AdView {
        val context = manager.context
        val future = CompletableFuture<AdView>()
        val adSize = when(manager.size){
            AdServerAdSize.FULL_BANNER -> AdSize.FULL_BANNER
            AdServerAdSize.LEADERBOARD -> AdSize.LEADERBOARD
            AdServerAdSize.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
            AdServerAdSize.LARGE_BANNER -> AdSize.LARGE_BANNER
            else -> AdSize.BANNER
        }
        lateinit var ad: AdView

        val listener = object : AdListener() {
            override fun onAdFailedToLoad(e: LoadAdError) {
                future.completeExceptionally(APIIOException(HTTPStatusCode.NO_RESPONSE, e.message))
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                manager.adId = "ADMOB_ADVIEW"
                manager.refreshRate = Ad.DEFAULT_AD_DURATION
                manager.ipAddress = InitializationManager.IP_ADDRESS
                future.complete(ad)
            }
        }

        context.runOnUiThread {
            ad = AdView(context)
            ad.setAdSize(adSize)
            ad.layoutParams = getAdLayoutParams(manager.orientation, manager.size)

            ad.adListener = listener
            ad.adUnitId = manager.unitId
            ad.loadAd(adRequest)
        }

        return future.get();
    }

}
