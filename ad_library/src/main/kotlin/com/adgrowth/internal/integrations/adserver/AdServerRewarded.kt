package com.adgrowth.internal.integrations.adserver

import android.app.Activity
import android.content.DialogInterface
import com.adgrowth.adserver.R
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.http.AdRequest
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.interfaces.integration.RewardedIntegration
import kotlin.math.ceil

class AdServerRewarded(unitId: String) :
    FullScreenAd<AdServerRewarded, AdServerRewarded.Listener>(),
    RewardedIntegration<AdServerRewarded, AdServerRewarded.Listener> {
    private var mRewarded = false

    init {
        mAdRequest = AdRequest(unitId!!)
    }

    public override fun onShow(dialogInterface: DialogInterface?) {
        super.onShow(dialogInterface)
        mDialog!!.showButtonText()
    }

    override fun load(context: Activity) {
        mRewarded = false
        Thread {
            try {
                super.requestAd(context, AdType.REWARDED)
            } catch (e: AdRequestException) {

                // TODO: Check if is no-ads-error and get an ad from integrations

                context.runOnUiThread { mListener!!.onFailedToLoad(e) }
            }
        }.start()
    }

    override fun dismiss() {
        super.dismiss()
        mContext!!.runOnUiThread { mListener!!.onDismissed() }
    }

    override fun setListener(listener: Listener) {
        mListener = listener
    }

    public override fun onDisplayTimeChanged(adStartedTime: Int) {
        super.onDisplayTimeChanged(adStartedTime)
        val remainingTime = ceil((TIME_TO_REWARD - adStartedTime).toDouble()).toInt()
        if (remainingTime < 0) {
            if (remainingTime <= TIME_TO_SHOW_TAP_TO_CLOSE) {
                mDialog!!.setButtonLabelText(mContext!!.getString(R.string.tap_to_close))
                stopAdStartedTimer()
            }
            return
        }
        var buttonLabel = mContext!!.resources.getQuantityString(
            R.plurals.remaining_seconds, remainingTime, remainingTime
        )
        if (remainingTime == 0) buttonLabel = mContext!!.getString(R.string.prize_received)
        mDialog!!.setButtonLabelText(buttonLabel)
        if (adStartedTime >= TIME_TO_REWARD && !mRewarded) {
            mRewarded = true
            mListener!!.onEarnedReward(ad!!.reward)
        }
    }

    interface Listener : RewardedIntegration.Listener<AdServerRewarded>
    companion object {
        private const val TIME_TO_REWARD = 30
        private const val TIME_TO_SHOW_TAP_TO_CLOSE = -3
    }
}
