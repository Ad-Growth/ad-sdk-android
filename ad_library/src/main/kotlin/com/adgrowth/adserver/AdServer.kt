package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.notifyProfileChanged
import com.adgrowth.internal.integrations.adserver.helpers.AdvertisingIdManager.getAdvertisingId
import com.adgrowth.internal.integrations.InitializationManager

object AdServer {

    @JvmStatic
    val isInitialized: Boolean
        get() = InitializationManager.isInitialized

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

    @JvmStatic
    private fun startSDK(context: Activity, clientKey: String, listener: Listener) {

        if (InitializationManager.isInitialized) {
            listener.onFailed(SDKInitException(SDKInitException.ALREADY_INITIALIZED))
            return
        }

        Thread { InitializationManager.ADVERTISING_ID = getAdvertisingId(context) }.start()
        Thread { InitializationManager(context, clientKey, clientProfile, listener) }.start()
    }


    interface Listener {
        fun onInit()
        fun onFailed(e: SDKInitException?)
    }
}
