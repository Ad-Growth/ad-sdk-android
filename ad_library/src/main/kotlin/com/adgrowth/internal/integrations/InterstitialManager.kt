package com.adgrowth.internal.integrations

import android.app.Activity
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.admob.AdMobInitializer
import com.adgrowth.internal.integrations.admob.AdMobInterstitial
import com.adgrowth.internal.integrations.adserver.AdServerInterstitial
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.interfaces.integrations.InterstitialIntegration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class InterstitialManager(
    private val mUnitId: String,
) : AdManager<InterstitialIntegration.Listener, InterstitialManager.Builder>() {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    var isLoaded = false
        private set
    var isFailed = false
        private set

    override lateinit var listener: InterstitialIntegration.Listener
    private var mAd: InterstitialIntegration? = null
    override val unitId: String
        get() {
            return when (builder) {
                // is AdColonyInterstitial.Builder -> SDKInitializer.appMetadata.adColony!!.interstitialUnitId!!
                // is UnityInterstitial.Builder -> SDKInitializer.appMetadata.unity!!.interstitialUnitId!!
                is AdMobInterstitial.Builder -> InitializationManager.APP_META_DATA.adMob!!.interstitialUnitId!!
                else -> {
                    if (InitializationManager.APP_META_DATA.isDevKey) return AdServerInterstitial.TEST_UNIT_ID
                    return mUnitId
                }
            }
        }

    init {
        integrations = InitializationManager.availableIntegrations.map {
            when (it) {
                // AdColonyInitializer::class.simpleName -> AdColonyInterstitial.Builder()
                // UnityInitializer::class.simpleName -> UnityInterstitial.Builder()
                AdMobInitializer::class.simpleName -> AdMobInterstitial.Builder()
                else -> AdServerInterstitial.Builder()
            }
        }

        builder = getNextIntegration()
    }

    fun load(context: Activity) {
        if (!InitializationManager.isInitialized) {
            throw AdRequestException(AdRequestException.SDK_NOT_INITIALIZED)
        }
        this.context = context

        ioScope.launch {
            while (mAd == null && builder != null) {

                isLoaded = false

                try {
                    mAd = builder!!.build(this@InterstitialManager).load(this@InterstitialManager)
                    isLoaded = true
                    listener.onLoad(mAd!!)
                    break
                } catch (e: APIIOException) {
                    if (e.statusCode == HTTPStatusCode.NOT_FOUND && e.message!!.contains("No ads found")) {
                        builder = getNextIntegration()
                        continue
                    }

                    isFailed = true
                    listener.onFailedToLoad(IOErrorHandler.handle(e))
                    break
                } catch (e: Exception) {
                    isFailed = true
                    listener.onFailedToLoad(IOErrorHandler.handle(e))
                    break
                }
            }

            if (builder == null) {
                isFailed = true
                listener.onFailedToLoad(AdRequestException(AdRequestException.NO_AD_FOUND))
            }
        }

    }

    fun show(context: Activity) {
        if (!AdServerEventManager.showPermission) {
            // ignore if it's already showing
            if (AdServerEventManager.adCurrentlyShown == hashCode()) return

            listener.onFailedToShow(AdRequestException.ALREADY_SHOWING_FULL_SCREEN_AD)
            return
        }
        this.context = context
        mAd?.show(this)
    }

    interface Builder {
        fun build(manager: InterstitialManager): InterstitialIntegration
    }

}
