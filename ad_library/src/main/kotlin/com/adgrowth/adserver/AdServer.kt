package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.notifyProfileChanged
import com.adgrowth.internal.integrations.adserver.helpers.AdvertisingIdManager.getAdvertisingId
import com.adgrowth.internal.integrations.SDKInitializer

object AdServer {
    @JvmStatic
    var clientKey: String = ""
        private set

    @JvmStatic
    var adId: String = ""
        private set


    @JvmStatic
    val isInitialized: Boolean
        get() = SDKInitializer.isInitialized


    @JvmStatic
    var clientProfile = ClientProfile()
        set(clientProfile) {
            field = clientProfile
            notifyProfileChanged(clientProfile)
        }

    @JvmStatic
    fun initialize(context: Activity, clientKey: String, listener: Listener) {
        startSDK(context, clientKey, listener)
    }

    @JvmStatic
    fun initialize(
        context: Activity, clientKey: String, profile: ClientProfile, listener: Listener
    ) {
        clientProfile = profile
        startSDK(context, clientKey, listener)
    }


    private fun startSDK(context: Activity, clientKey: String, listener: Listener) {

        if (SDKInitializer.isInitialized) {
            listener.onFailed(SDKInitException(SDKInitException.ALREADY_INITIALIZED))
            return
        }
        if (clientKey == AdServer.clientKey) {
            listener.onInit()
            return
        }
        AdServer.clientKey = clientKey

        Thread { adId = getAdvertisingId(context) }.start()

        SDKInitializer(context, AdServer.clientKey, listener)
    }


    interface Listener {
        fun onInit()
        fun onFailed(e: SDKInitException?)
    }
}
