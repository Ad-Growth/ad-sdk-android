package com.adgrowth.internal.integrations

import android.app.Activity
import com.adgrowth.adserver.entities.RewardItem
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.admob.AdMobInitializer
import com.adgrowth.internal.integrations.admob.AdMobRewarded
import com.adgrowth.internal.integrations.adserver.AdServerRewarded
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import com.adgrowth.internal.integrations.adserver.helpers.JSONHelper
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.interfaces.integrations.RewardedIntegration


class RewardedManager(
    private val mUnitId: String,
) : AdManager<RewardedIntegration.Listener, RewardedManager.Builder>() {
    var isLoaded = false
        private set
    var isFailed = false
        private set

    override lateinit var listener: RewardedIntegration.Listener
    private var mAd: RewardedIntegration? = null

    override val unitId: String
        get() {
            return when (builder) {
                // is AdColonyRewarded.Builder -> SDKInitializer.appMetadata.adColony!!.rewardedUnitId!!
                // is UnityRewarded.Builder -> SDKInitializer.appMetadata.unity!!.rewardedUnitId!!
                is AdMobRewarded.Builder -> InitializationManager.APP_META_DATA.adMob!!.rewardedUnitId!!
                else -> {
                    if (InitializationManager.APP_META_DATA.isDevKey) return AdServerRewarded.TEST_UNIT_ID
                    return mUnitId
                }
            }
        }

    init {
        integrations = InitializationManager.availableIntegrations.map {
            when (it) {
                // AdColonyInitializer::class.simpleName -> AdColonyRewarded.Builder()
                // UnityInitializer::class.simpleName -> UnityRewarded.Builder()
                AdMobInitializer::class.simpleName -> AdMobRewarded.Builder()
                else -> AdServerRewarded.Builder()
            }
        }
        builder = getNextIntegration()
    }

    fun load(context: Activity) {
        if (!InitializationManager.isInitialized) {
            throw AdRequestException(AdRequestException.SDK_NOT_INITIALIZED)
        }
        this.context = context

        Thread {
            while (mAd == null && builder != null) {

                this.isLoaded = false

                try {
                    mAd = builder!!.build(this).load(this)
                    isLoaded = true
                    listener.onLoad(mAd!!)
                    break
                } catch (e: APIIOException) {
                    if (e.statusCode == HTTPStatusCode.NOT_FOUND && e.message!!.contains("No ads found")) {

                        val meta = JSONHelper.safeGetObject(e.body, "meta")

                        if (meta.has("reward_item") && meta.has("reward_value")) {
                            reward = RewardItem(
                                meta.getInt("reward_value"),
                                meta.getString("reward_item"),
                            )
                        }

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
        }.start()

    }


    fun show(context: Activity) {
        if (!AdServerEventManager.showPermission) {
            listener.onFailedToShow(AdRequestException.ALREADY_SHOWING_FULL_SCREEN_AD)
            return
        }
        this.context = context
        mAd?.show(this)
    }

    interface Builder {
        fun build(manager: RewardedManager): RewardedIntegration
    }
}
