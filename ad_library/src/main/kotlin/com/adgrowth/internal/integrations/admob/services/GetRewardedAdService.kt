package com.adgrowth.internal.integrations.admob.services

import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.RewardedManager
import com.adgrowth.internal.integrations.InitializationManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService


class GetRewardedAdService(override val manager: RewardedManager) :
    IGetAdService<RewardedAd>(manager) {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    override fun run(adRequest: AdRequest): RewardedAd {
        val context = manager.context
        val future = CompletableFuture<RewardedAd>()

        val listener = object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(e: LoadAdError) {
                future.completeExceptionally(APIIOException(HTTPStatusCode.NO_RESPONSE, e.message))
            }

            override fun onAdLoaded(ad: RewardedAd) {
                manager.adId = "ADMOB_REWARDED"
                manager.ipAddress = InitializationManager.IP_ADDRESS
                future.complete(ad)
            }
        }

        mainScope.launch {
            RewardedAd.load(context, manager.unitId, adRequest, listener)
        }

        return future.get();
    }

}
