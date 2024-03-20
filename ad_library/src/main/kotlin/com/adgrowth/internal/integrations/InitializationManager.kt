package com.adgrowth.internal.integrations

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.admob.AdMobInitializer
import com.adgrowth.internal.integrations.adserver.AdServerInitializer
import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager


class InitializationManager(
    override val context: Activity,
    override val clientProfile: ClientProfile,
    override var listener: AdServer.Listener
) : IInitializationManager(context, clientProfile, listener) {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    override val appMetadata: AppMetaData
        get() = APP_META_DATA

    init {
        try {
            APP_PACKAGE_NAME = context.packageName
            CLIENT_KEY = getClientKey(context)

            val adServer = AdServerInitializer.Builder().build(this).initialize()

            availableIntegrations.add(adServer.javaClass.name)

            APP_META_DATA = (adServer as AdServerInitializer).appMetadata
            IP_ADDRESS = appMetadata.ipAddress

            /** Add third party integrations here */

            // Google AdMob
            if (APP_META_DATA.adMob?.appId != null) {
                val admob = AdMobInitializer.Builder().build(this).initialize()
                availableIntegrations.add(admob.javaClass.simpleName)
            }

            // Unity
            // if (adServer.appMetadata.unityAppId != null) {
            //      val unity = UnityInitializer(context, appMetadata.unityAppId).initialize(thirdPartyListener)
            //      availableIntegrations.add(unity.javaClass.simpleName)
            // }

            // AdColony
            // if (adServer.appMetadata.adColonyAppId != null) {
            //     AdColonyInitializer.Builder().build(manager).initialize(thirdPartyListener)
            // }

            notifyInitialized()
        } catch (e: APIIOException) {
            when (e.statusCode) {
                HTTPStatusCode.NO_CONTENT -> notifyFailed(SDKInitException(e.message))
                else -> notifyFailed(SDKInitException(SDKInitException.UNKNOWN_ERROR))

            }
        } catch (e: AdRequestException) {
            notifyFailed(SDKInitException(SDKInitException.UNKNOWN_ERROR))
        } catch (e: Exception) {
            notifyFailed(SDKInitException(SDKInitException.UNKNOWN_ERROR))
        }

    }

    private fun getClientKey(context: Activity): String {

        val app: ApplicationInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )

        val string = app.metaData.getString(ADSERVER_META_KEY)

        if (string.isNullOrEmpty()) throw Throwable("client_key meta tag missing from your AndroidManifest.xml")

        return string
    }

    private fun notifyInitialized() {
        isInitialized = true
        mainScope.launch {
            listener.onInit()
        }
        AdServerEventManager.notifySDKInitialized()
    }

    private fun notifyFailed(exception: SDKInitException) {
        mainScope.launch {
            listener.onFailed(exception);
        }
    }

    companion object {
        var isInitialized: Boolean = false
        val availableIntegrations = ArrayList<String>()
        private const val ADSERVER_META_KEY = "com.adgrowth.adserver.CLIENT_KEY"
        lateinit var APP_META_DATA: AppMetaData
        var CLIENT_KEY: String = ""
        var ADVERTISING_ID: String = ""
        var IP_ADDRESS: String = "127.0.0.1"
        var APP_PACKAGE_NAME = ""
    }
}
