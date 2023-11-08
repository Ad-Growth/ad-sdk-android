package com.adgrowth.adserver


import android.app.Activity
import com.adgrowth.adserver.entities.RewardItem
import com.adgrowth.adserver.exceptions.AdRequestException

import com.adgrowth.internal.integrations.adserver.AdServerRewarded
import com.adgrowth.internal.interfaces.integration.RewardedIntegration

class RewardedAd(unitId: String) : AdServerRewarded.Listener {

    private lateinit var mListener: Listener
    var mAd: AdServerRewarded

    init {
        mAd = AdServerRewarded(unitId)
    }

    fun setListener(listener: Listener) {
        this.mListener = listener;
        mAd.setListener(this)
    }

    fun show(context: Activity) {
        mAd.show(context);
    }

    fun load(context: Activity) {
        mAd.load(context)
    }

    fun isLoaded(): Boolean {
        return mAd.isLoaded()
    }

    fun isFailed(): Boolean {
        return mAd.isFailed()
    }


    override fun onDismissed() {
        mListener.onDismissed()
    }

    override fun onLoad(ad: AdServerRewarded) {
        mListener.onLoad(this)
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        mListener.onFailedToLoad(exception)
    }

    override fun onClicked() {
        mListener.onClicked()
    }

    override fun onFailedToShow(code: String?) {
        mListener.onFailedToShow(code)
    }

    override fun onImpression() {
        mListener.onImpression()
    }

    override fun onEarnedReward(rewardItem: RewardItem?) {
        mListener.onEarnedReward(rewardItem)
    }


    interface Listener : RewardedIntegration.Listener<RewardedAd>
}
