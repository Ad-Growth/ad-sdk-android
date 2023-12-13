package com.adgrowth.internal.integrations

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.admob.AdMobAdView
import com.adgrowth.internal.integrations.admob.AdMobInitializer
import com.adgrowth.internal.integrations.adserver.AdServerAdView

import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.showPermission
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import com.adgrowth.internal.integrations.adserver.helpers.JSONHelper
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.interfaces.integrations.AdViewIntegration
import java.util.*

class AdViewManager(
    private val mUnitId: String,
) : AdManager<AdViewIntegration.Listener, AdViewManager.Builder>(),
    Application.ActivityLifecycleCallbacks, AdServerEventManager.FullScreenListener {
    private var mAd: AdViewIntegration? = null
    override lateinit var listener: AdViewIntegration.Listener

    private var mRefreshTimer: Timer? = null
    var isFailed = false
        private set
    private var mCurrentRefreshTime: Int = 0
    lateinit var size: AdSize
    lateinit var orientation: AdOrientation

    override val unitId: String
        get() {
            return when (builder) {
                // is AdColonyAdView.Builder -> SDKInitializer.appMetadata.adColony!!.bannerUnitId!!
                // is UnityAdView.Builder -> SDKInitializer.appMetadata.unity!!.bannerUnitId!!
                is AdMobAdView.Builder -> InitializationManager.APP_META_DATA.adMob!!.bannerUnitId!!
                else -> return mUnitId
            }
        }


    init {
        integrations = InitializationManager.availableIntegrations.map {
            when (it) {
                // AdColonyInitializer::class.simpleName -> AdColonyAdView.Builder()
                // UnityInitializer::class.simpleName -> UnityAdView.Builder()
                AdMobInitializer::class.simpleName -> AdMobAdView.Builder()
                else -> AdServerAdView.Builder()
            }
        }
        builder = getNextIntegration()
    }

    fun load(context: Activity, size: AdSize, orientation: AdOrientation) {
        if (!InitializationManager.isInitialized) {
            throw AdRequestException(AdRequestException.SDK_NOT_INITIALIZED)
        }
        stopRefreshTimer()
        mCurrentRefreshTime = 0
        this.context = context
        this.size = size
        this.orientation = orientation
        this.mAd = null

        Thread {
            while (mAd == null && builder != null) {
                try {
                    mAd = builder!!.build(this).load(this)
                    listener.onLoad(mAd!!)
                    break
                } catch (e: APIIOException) {
                    if (e.statusCode == HTTPStatusCode.NOT_FOUND && e.message!!.contains("No ads found")) {

                        val meta = JSONHelper.safeGetObject(e.body, "meta")

                        if (meta.has("refresh_rate")) {
                            refreshRate = JSONHelper.safeGetInt(meta, "refresh_rate", null)
                        }

                        builder = getNextIntegration()
                        continue
                    }

                    refreshRate = AFTER_ERROR_REFRESH_RATE
                    startRefreshTimer()

                    isFailed = true
                    listener.onFailedToLoad(IOErrorHandler.handle(e))
                    break
                } catch (e: Exception) {
                    refreshRate = AFTER_ERROR_REFRESH_RATE
                    startRefreshTimer()

                    isFailed = true
                    listener.onFailedToLoad(IOErrorHandler.handle(e))
                    break
                }
            }

            if (builder == null) {
                isFailed = true
                listener.onFailedToLoad(AdRequestException(AdRequestException.NO_AD_FOUND))
            }
        }.start()
    }


    fun show(parent: ViewGroup) {

        context.application.registerActivityLifecycleCallbacks(this)
        AdServerEventManager.registerFullScreenListener(this)

        context.runOnUiThread {
            stopRefreshTimer()
            mAd!!.placeIn(parent)
            mCurrentRefreshTime = 0
            startRefreshTimer()
        }
    }

    fun reload(parent: ViewGroup) {
        context.runOnUiThread {
            stopRefreshTimer()
            if (parent.indexOfChild(mAd) >= 0) parent.removeView(mAd)
            load(context, size, orientation)
        }
    }

    private fun startRefreshTimer() {
        mRefreshTimer?.cancel()

        if (refreshRate == Ad.DISABLED_REFRESH_RATE) return

        mRefreshTimer = Timer()

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                mCurrentRefreshTime++
                if (mCurrentRefreshTime >= refreshRate!!) {
                    stopRefreshTimer()
                    listener.onFinished()
                }

            }
        }
        mRefreshTimer!!.scheduleAtFixedRate(task, 1000, 1000)
    }


    private fun stopRefreshTimer() {
        if (mRefreshTimer != null) {
            mRefreshTimer!!.cancel()
            mRefreshTimer = null
        }
    }

    fun release() {
        stopRefreshTimer()
        context.application.unregisterActivityLifecycleCallbacks(this)
        AdServerEventManager.unregisterFullScreenListener(this)
    }


    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (AdServerEventManager.isSameActivity(context, activity) && showPermission) {
            startRefreshTimer()
            mAd?.resumeAd()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (AdServerEventManager.isSameActivity(context, activity)) {
            stopRefreshTimer()
            mAd?.pauseAd()
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (AdServerEventManager.isSameActivity(context, activity)) {
            stopRefreshTimer()
            AdServerEventManager.unregisterFullScreenListener(this)
            context.application?.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onFullScreenShown(instanceHash: Int) {
        stopRefreshTimer()
        mAd?.pauseAd()
    }

    override fun onFullScreenDismissed() {
        startRefreshTimer()
        mAd?.resumeAd()
    }


    interface Builder {
        fun build(manager: AdViewManager): AdViewIntegration
    }

    companion object {
        private const val AFTER_ERROR_REFRESH_RATE = 10
    }

}
