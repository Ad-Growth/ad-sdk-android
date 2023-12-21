package com.adgrowth.internal.integrations.adserver

import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.integrations.adserver.helpers.ScreenHelpers
import com.adgrowth.internal.integrations.adserver.services.GetAdService
import com.adgrowth.internal.integrations.adserver.services.SendAdEventService
import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService as ISendAdEventService
import com.adgrowth.internal.interfaces.integrations.InterstitialIntegration
import java.util.concurrent.CompletableFuture

class AdServerInterstitial(
    private val getAdService: IGetAdService, sendAdEventService: ISendAdEventService
) : InterstitialIntegration,
    FullScreenAd<InterstitialIntegration, InterstitialIntegration.Listener>(sendAdEventService) {

    override fun load(manager: InterstitialManager): InterstitialIntegration {
        mLoadFuture = CompletableFuture()
        mListener = manager.listener
        mContext = manager.context
        mFailedToLoad = false

        super.beforeLoadCheck()

        val options = HashMap<String, Any>()
        options["orientation"] = ScreenHelpers.getOrientation(mContext).toString()

        mAd = getAdService.run(options)

        super.afterLoadCheck(mAd, AdType.INTERSTITIAL)

        super.prepareAdMedia(mAd)
        return mLoadFuture.get()
    }

    override fun show(manager: InterstitialManager) {
        super.show(manager)
    }


    override fun dismiss() {
        mContext.runOnUiThread { mListener?.onDismissed() }
        super.dismiss()
    }

    override fun setListener(listener: InterstitialIntegration.Listener) {
        mListener = listener
    }

    override fun onRunningTimeChanged(elapsedTime: Int) {
        super.onRunningTimeChanged(elapsedTime)
    }

    class Builder : InterstitialManager.Builder {
        override fun build(manager: InterstitialManager): InterstitialIntegration {
            return AdServerInterstitial(
                makeGetAdService(manager), makeSendAdEventService(manager)
            )
        }

        private fun makeGetAdService(manager: InterstitialManager): IGetAdService {
            return GetAdService(manager)
        }

        private fun makeSendAdEventService(manager: InterstitialManager): ISendAdEventService {
            return SendAdEventService(manager)
        }
    }

    companion object {
        const val TEST_UNIT_ID: String = "interstitial"
    }
}
