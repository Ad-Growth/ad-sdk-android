package com.adgrowth.internal.integrations.admob.services

import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.integrations.InitializationManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService


class GetInterstitialAdService(override val manager: InterstitialManager) :
    IGetAdService<InterstitialAd>(manager) {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    override fun run(adRequest: AdRequest): InterstitialAd {
        val context = manager.context
        val future = CompletableFuture<InterstitialAd>()

        val listener = object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(e: LoadAdError) {
                future.completeExceptionally(APIIOException(HTTPStatusCode.NO_RESPONSE, e.message))
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                manager.adId = "ADMOB_INTERSTITIAL"
                manager.ipAddress = InitializationManager.IP_ADDRESS
                future.complete(ad)
            }
        }

        mainScope.launch {
            InterstitialAd.load(context, manager.unitId, adRequest, listener)
        }

        return future.get();
    }

}
