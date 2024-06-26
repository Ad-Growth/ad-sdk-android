package com.adgrowth.internal.integrations.adserver.helpers

import android.app.Activity
import android.content.Context
import com.adgrowth.adserver.entities.ClientProfile

/**
 * This class handle internal events on AdServer
 */
object AdServerEventManager {
    var adCurrentlyShown: Int? = null
    private val initializeListeners: MutableList<SdkInitializedListener> = ArrayList()
    private val fullScreenListeners: MutableList<FullScreenListener> = ArrayList()
    private val clientProfileListener: MutableList<ClientProfileListener> = ArrayList()

    @JvmStatic
    val showPermission: Boolean
        get() = adCurrentlyShown == null

    @JvmStatic
    var sdkInitialized: Boolean = false
        private set

    @JvmStatic
    fun registerFullScreenListener(listener: FullScreenListener) {
        fullScreenListeners.add(listener)
    }

    @JvmStatic
    fun unregisterFullScreenListener(listener: FullScreenListener) {
        fullScreenListeners.remove(listener)
    }

    @JvmStatic
    fun notifyFullScreenShown(instanceHash: Int) {
        adCurrentlyShown = instanceHash
        for (listener in fullScreenListeners) {
            listener.onFullScreenShown(instanceHash)
        }
    }

    @JvmStatic
    fun notifyFullScreenDismissed() {
        adCurrentlyShown = null
        for (listener in fullScreenListeners) {
            listener.onFullScreenDismissed()
        }
    }


    @JvmStatic
    fun registerClientProfileListener(listener: ClientProfileListener) {
        clientProfileListener.add(listener)
    }

    @JvmStatic
    fun unregisterClientProfileListener(listener: ClientProfileListener) {
        clientProfileListener.remove(listener)
    }

    @JvmStatic
    fun notifyProfileChanged(profile: ClientProfile) {
        for (listener in clientProfileListener) {
            listener.onProfileChanged(profile)
        }
    }

    @JvmStatic
    fun registerSDKInitializedListener(listener: SdkInitializedListener) {
        initializeListeners.add(listener)
    }

    @JvmStatic
    fun unregisterSDKInitializedListener(listener: SdkInitializedListener) {
        initializeListeners.remove(listener)
    }

    @JvmStatic
    fun notifySDKInitialized() {
        sdkInitialized = true
        for (listener in initializeListeners) {
            listener.onSDKInit()
        }
    }

    fun isSameActivity(context: Context, activity: Activity): Boolean {
        return context == activity
    }


    interface FullScreenListener {
        fun onFullScreenShown(instanceHash: Int);
        fun onFullScreenDismissed();
    }

    interface SdkInitializedListener {
        fun onSDKInit()
    }

    interface ClientProfileListener {
        fun onProfileChanged(profile: ClientProfile)
    }


}
