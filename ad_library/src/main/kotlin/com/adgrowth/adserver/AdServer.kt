package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.notifyProfileChanged
import com.adgrowth.internal.integrations.adserver.helpers.AdvertisingIdManager.getAdvertisingId
import com.adgrowth.internal.integrations.InitializationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdServer {
    private val ioScope = CoroutineScope(Dispatchers.IO)

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
    fun initialize(context: Activity, listener: Listener) {
        startSDK(context, listener)
    }

    @JvmStatic
    fun initialize(
        context: Activity, profile: ClientProfile, listener: Listener
    ) {
        clientProfile = profile
        startSDK(context, listener)
    }

    @JvmStatic
    private fun startSDK(context: Activity, listener: Listener) {

        if (InitializationManager.isInitialized) {
            listener.onFailed(SDKInitException(SDKInitException.ALREADY_INITIALIZED))
            return
        }

        ioScope.launch {
            InitializationManager.ADVERTISING_ID = getAdvertisingId(context)
            InitializationManager(context, clientProfile, listener)
        }
    }


    interface Listener {
        fun onInit()
        fun onFailed(e: SDKInitException?)
    }
}
