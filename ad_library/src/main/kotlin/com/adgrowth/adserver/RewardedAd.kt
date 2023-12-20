package com.adgrowth.adserver


import android.app.Activity
import com.adgrowth.adserver.entities.RewardItem
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.RewardedManager
import com.adgrowth.internal.interfaces.integrations.RewardedIntegration

class RewardedAd(unitId: String) : RewardedIntegration.Listener {
    private lateinit var mContext: Activity
    private lateinit var mListener: Listener
    private var mAdManager: RewardedManager

    init {
        mAdManager = RewardedManager(unitId)
        mAdManager.listener = this
    }

    fun setListener(listener: Listener) {
        this.mListener = listener;
    }

    fun show(context: Activity) {
        mContext = context;
        if (!mAdManager.isLoaded) {
            mListener.onFailedToShow(AdRequestException.NOT_READY)
            return
        }
        mAdManager.show(context);
    }

    fun load(context: Activity) {
        mContext = context;
        mAdManager.load(context)
    }

    fun isLoaded(): Boolean {
        return mAdManager.isLoaded
    }

    fun isFailed(): Boolean {
        return mAdManager.isFailed
    }

    override fun onDismissed() {
        mContext.runOnUiThread { mListener.onDismissed() }
    }

    override fun onLoad(ad: RewardedIntegration) {
        mContext.runOnUiThread { mListener.onLoad(this) }
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        mContext.runOnUiThread { mListener.onFailedToLoad(exception) }
    }

    override fun onClicked() {
        mContext.runOnUiThread { mListener.onClicked() }
    }

    override fun onFailedToShow(code: String?) {
        mContext.runOnUiThread { mListener.onFailedToShow(code) }
    }

    override fun onImpression() {
        mContext.runOnUiThread { mListener.onImpression() }
    }

    override fun onEarnedReward(rewardItem: RewardItem) {
        mContext.runOnUiThread { mListener.onEarnedReward(rewardItem) }
    }


    interface Listener {
        fun onLoad(ad: RewardedAd)
        fun onFailedToLoad(exception: AdRequestException?)
        fun onEarnedReward(rewardItem: RewardItem)

        @JvmDefault
        fun onDismissed() {
        }

        @JvmDefault
        fun onClicked() {
        }

        @JvmDefault
        fun onFailedToShow(code: String?) {
        }

        @JvmDefault
        fun onImpression() {
        }
    }
}
