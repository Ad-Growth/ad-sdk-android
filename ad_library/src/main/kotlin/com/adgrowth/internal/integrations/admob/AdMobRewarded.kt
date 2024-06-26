package com.adgrowth.internal.integrations.admob

import android.app.Activity
import com.adgrowth.adserver.AdServer
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.integrations.RewardedManager
import com.adgrowth.internal.integrations.admob.services.GetRewardedAdService
import com.adgrowth.internal.integrations.admob.services.SendAdEventService
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.interfaces.integrations.RewardedIntegration
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.admob.services.interfaces.SendAdEventService as ISendAdEventService

class AdMobRewarded(
    private val getAdService: IGetAdService<RewardedAd>,
    private val sendAdEventService: ISendAdEventService
) : RewardedIntegration, FullScreenContentCallback() {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mContext: Activity
    private lateinit var manager: RewardedManager
    private var mAd: RewardedAd? = null
    private var mListener: RewardedIntegration.Listener? = null

    override fun show(manager: RewardedManager) {
        this.manager = manager
        mContext = manager.context
        mainScope.launch {
            mAd!!.show(mContext) {
                mListener?.onEarnedReward(manager.reward)
            }
        }
    }

    override fun load(manager: RewardedManager): AdMobRewarded {
        this.manager = manager
        mContext = manager.context
        mListener = manager.listener

        val adRequest = AdRequest.Builder()
        val profile = AdServer.clientProfile

        profile.interests.forEach { interest ->
            adRequest.addKeyword(interest)
        }

        mAd = getAdService.run(adRequest.build());
        mAd!!.fullScreenContentCallback = this
        mListener?.onLoad(this)
        return this
    }

    override fun onRunningTimeChanged(elapsedTime: Int) {
    }

    override fun setListener(listener: RewardedIntegration.Listener) {
        mListener = listener;
    }

    override fun onAdClicked() {
        super.onAdClicked()
        sendAdEventService.run(AdEventType.CLICK)
        mListener?.onClicked()
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        AdServerEventManager.notifyFullScreenDismissed()
        mListener?.onDismissed()
    }

    override fun onAdFailedToShowFullScreenContent(e: AdError) {
        super.onAdFailedToShowFullScreenContent(e)
        mListener?.onFailedToShow(e.message)
    }

    override fun onAdImpression() {
        super.onAdImpression()
        AdServerEventManager.notifyFullScreenShown(manager.hashCode())
        sendAdEventService.run(AdEventType.VIEW)
        mListener?.onImpression()
    }

    class Builder : RewardedManager.Builder {
        override fun build(manager: RewardedManager): RewardedIntegration {
            return AdMobRewarded(
                makeGetAdService(manager), makeSendAdEventService(manager)
            )
        }

        private fun makeGetAdService(manager: RewardedManager): IGetAdService<RewardedAd> {
            return GetRewardedAdService(manager)
        }

        private fun makeSendAdEventService(manager: RewardedManager): ISendAdEventService {
            return SendAdEventService(manager)
        }
    }

}
