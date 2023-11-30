package com.adgrowth.internal.integrations.admob.services

import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager
import com.adgrowth.internal.integrations.admob.services.interfaces.AdMobInitializeService as IAdMobInitializeService
import com.google.android.gms.ads.MobileAds

import java.util.concurrent.CompletableFuture

class AdMobInitializeService(override val manager: IInitializationManager) :
    IAdMobInitializeService(manager) {
    override fun run() {
        val future = CompletableFuture<Unit>()

        MobileAds.initialize(manager.context) { future.complete(Unit) }

        return future.get()
    }
}
