package com.adgrowth.internal.integrations.admob

import android.app.Activity
import com.adgrowth.adserver.AdServer
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.integrations.admob.services.GetInterstitialAdService
import com.adgrowth.internal.integrations.admob.services.SendAdEventService
import com.adgrowth.internal.interfaces.integrations.InterstitialIntegration
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.admob.services.interfaces.SendAdEventService as ISendAdEventService

class AdMobInterstitial(
    private val getAdService: IGetAdService<InterstitialAd>,
    private val sendAdEventService: ISendAdEventService
) : InterstitialIntegration, FullScreenContentCallback() {

    private lateinit var mContext: Activity
    private var mAd: InterstitialAd? = null
    private var mListener: InterstitialIntegration.Listener? = null


    override fun show(manager: InterstitialManager) {
        mContext = manager.context;
        mContext.runOnUiThread {
            mAd!!.show(mContext);
        }
    }

    override fun load(manager: InterstitialManager): AdMobInterstitial {
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

    override fun onRunningTimeChanged(elapsedTime: Int) {}

    override fun setListener(listener: InterstitialIntegration.Listener) {
        mListener = listener
    }

    override fun onAdClicked() {
        super.onAdClicked()
        sendAdEventService.run(AdEventType.CLICK)
        mListener?.onClicked()
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        mListener?.onDismissed()
    }

    override fun onAdFailedToShowFullScreenContent(e: AdError) {
        super.onAdFailedToShowFullScreenContent(e)
        mListener?.onFailedToShow(e.message)
    }

    override fun onAdImpression() {
        super.onAdImpression()
        sendAdEventService.run(AdEventType.VIEW)
        mListener?.onImpression()
    }

    class Builder : InterstitialManager.Builder {
        override fun build(manager: InterstitialManager): InterstitialIntegration {
            return AdMobInterstitial(
                makeGetAdService(manager), makeSendAdEventService(manager)
            )
        }

        private fun makeGetAdService(manager: InterstitialManager): IGetAdService<InterstitialAd> {
            return GetInterstitialAdService(manager)
        }

        private fun makeSendAdEventService(manager: InterstitialManager): ISendAdEventService {
            return SendAdEventService(manager)
        }
    }


}
