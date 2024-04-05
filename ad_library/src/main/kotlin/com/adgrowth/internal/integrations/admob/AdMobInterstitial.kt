package com.adgrowth.internal.integrations.admob

import android.app.Activity
import com.adgrowth.adserver.AdServer
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.integrations.admob.services.GetInterstitialAdService
import com.adgrowth.internal.integrations.admob.services.SendAdEventService
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.interfaces.integrations.InterstitialIntegration
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.admob.services.interfaces.SendAdEventService as ISendAdEventService

class AdMobInterstitial(
    private val getAdService: IGetAdService<InterstitialAd>,
    private val sendAdEventService: ISendAdEventService
) : InterstitialIntegration, FullScreenContentCallback() {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mContext: Activity
    private lateinit var manager: InterstitialManager
    private var mAd: InterstitialAd? = null
    private var mListener: InterstitialIntegration.Listener? = null


    override fun show(manager: InterstitialManager) {
        this.manager = manager
        mContext = manager.context;
        mainScope.launch {
            mAd!!.show(mContext);
        }
    }

    override fun load(manager: InterstitialManager): AdMobInterstitial {
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
        AdServerEventManager.notifyFullScreenDismissed()
        mListener?.onDismissed()
    }

    override fun onAdFailedToShowFullScreenContent(e: AdError) {
        super.onAdFailedToShowFullScreenContent(e)
        mListener?.onFailedToShow(e.message)
    }

    override fun onAdImpression() {
        AdServerEventManager.notifyFullScreenShown(manager.hashCode())
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
