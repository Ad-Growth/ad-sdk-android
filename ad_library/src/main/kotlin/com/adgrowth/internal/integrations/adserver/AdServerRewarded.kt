package com.adgrowth.internal.integrations.adserver


import com.adgrowth.adserver.BuildConfig
import com.adgrowth.adserver.R
import com.adgrowth.adserver.helpers.LayoutHelpers
import com.adgrowth.internal.integrations.InitializationManager
import com.adgrowth.internal.integrations.RewardedManager
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.adserver.services.GetAdService
import com.adgrowth.internal.integrations.adserver.services.SendAdEventService
import com.adgrowth.internal.interfaces.integrations.RewardedIntegration
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil
import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService as ISendAdEventService


class AdServerRewarded(
    private val getAdService: IGetAdService, sendAdEventService: ISendAdEventService
) : RewardedIntegration,
    FullScreenAd<RewardedIntegration, RewardedIntegration.Listener>(sendAdEventService) {
    private var mRewarded = false


    override fun onShow() {
        super.onShow()
        mDialog?.showButtonText()
    }

    override fun load(manager: RewardedManager): RewardedIntegration {
        mLoadFuture = CompletableFuture()
        mListener = manager.listener
        mContext = manager.context
        mFailedToLoad = false

        super.beforeLoadCheck()

        val options = HashMap<String, Any>()
        options["orientation"] = LayoutHelpers.getAdOrientation().toString()

        mAd = getAdService.run(options)

        super.afterLoadCheck(mAd, AdType.REWARDED)

        super.prepareAdMedia(mAd)
        return mLoadFuture.get()
    }

    override fun show(manager: RewardedManager) {
        super.show(manager)
    }

    override fun setListener(listener: RewardedIntegration.Listener) {
        mListener = listener
    }

    override fun onRunningTimeChanged(elapsedTime: Int) {
        super.onRunningTimeChanged(elapsedTime)

        val remainingTime = ceil((TIME_TO_REWARD - elapsedTime).toDouble()).toInt()

        if (remainingTime < 0) {
            if (remainingTime <= TIME_TO_SHOW_TAP_TO_CLOSE) {
                mDialog?.setButtonLabelText(mContext.getString(R.string.tap_to_close))
                stopRunningTimer()
            }
            return
        }

        var buttonLabel = mContext.resources.getQuantityString(
            R.plurals.remaining_seconds, remainingTime, remainingTime
        )
        if (remainingTime == 0) buttonLabel = mContext.getString(R.string.prize_received)

        mDialog?.setButtonLabelText(buttonLabel)

        if (elapsedTime >= TIME_TO_REWARD && !mRewarded) {
            mRewarded = true
            mListener!!.onEarnedReward(mAd.reward)
        }
    }

    class Builder : RewardedManager.Builder {
        override fun build(manager: RewardedManager): RewardedIntegration {
            return AdServerRewarded(
                makeGetAdService(manager), makeSendAdEventService(manager)
            )
        }

        private fun makeGetAdService(manager: RewardedManager): IGetAdService {
            return GetAdService(manager)
        }

        private fun makeSendAdEventService(manager: RewardedManager): ISendAdEventService {
            return SendAdEventService(manager)
        }
    }

    companion object {
        const val TEST_UNIT_ID: String = "rewarded"
        private val TIME_TO_REWARD: Double
            get() {
                if (InitializationManager.APP_META_DATA.isDevKey || BuildConfig.DEBUG) return 10.0
                return 30.0
            }
        private const val TIME_TO_SHOW_TAP_TO_CLOSE = -3
    }
}
