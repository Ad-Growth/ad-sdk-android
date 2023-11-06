package com.adgrowth.internal.integrations

import android.app.Activity
import android.util.Log
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.integrations.adserver.enums.IntegrationType
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.admob.AdMobInitializer
import com.adgrowth.internal.integrations.adserver.AdServerInitializer


class SDKInitializer
    (private val context: Activity, clientKey: String, private var listener: AdServer.Listener) {

    private var adServer: AdServerInitializer = AdServerInitializer(context, clientKey)
    private lateinit var adMob: AdMobInitializer


    // AdServer
    private val adServerListener = object : AdServerInitializer.Listener {
        override fun onInit(initializer: AdServerInitializer) {
            isInitialized = true
            notifyInitialized()

            /** Add integrations here */
            if (adServer.appMetadata != null) {

                // Google AdMob
                if (adServer.appMetadata!!.adMobAppId != null) {
                    adMob = AdMobInitializer(context, adServer.appMetadata!!.adMobAppId)
                    adMob.initialize(adMobListener)
                }

                // Unity
                // if (adServer.appMetadata!!.unityAppId != null) {
                //      unity = UnityInitializer(context, adServer.appMetadata.unityAppId)
                //      unity.initialize(unityListener)
                // }

                // AdColony
                // if (adServer.appMetadata!!.adColonyAppId != null) {
                //     AdColony = AdColonyInitializer(context, adServer.appMetadata.adColonyAppId)
                //     AdColony.initialize(adColonyListener)
                // }

            }
        }

        override fun onFailed(e: SDKInitException) {
            notifyFailed(e)
        }
    }

    // Third-party
    private val adMobListener = object : AdMobInitializer.Listener {
        override fun onInit(initializer: AdMobInitializer) {
            availableIntegrations.add(IntegrationType.AdMob)
        }

        override fun onFailed(e: String) {
            // ignore, third-party is not important.. maybe print a warning?
            Log.w("AdServer", " Failed to initialize integration")

            if (availableIntegrations.contains(IntegrationType.AdMob)) {
                availableIntegrations.remove(IntegrationType.AdMob)
            }
        }
    }

    init {
        adServer.initialize(adServerListener)
    }

    fun notifyInitialized() {
        isInitialized = true
        AdServerEventManager.notifySDKInitialized()
        context.runOnUiThread {
            listener.onInit()
        }
    }

    fun notifyFailed(exception: SDKInitException) {
        context.runOnUiThread {
            listener.onFailed(exception);
        }
    }

    companion object {
        @JvmField
        var isInitialized: Boolean = false
        private val availableIntegrations = ArrayList<IntegrationType>();
    }
}
