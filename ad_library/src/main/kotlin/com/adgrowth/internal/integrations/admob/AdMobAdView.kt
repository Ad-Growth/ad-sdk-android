package com.adgrowth.internal.integrations.admob

import android.os.Handler
import android.view.ViewGroup
import com.adgrowth.adserver.AdServer
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.helpers.LayoutHelper
import com.adgrowth.internal.integrations.AdViewManager
import com.adgrowth.internal.integrations.admob.services.GetAdViewService
import com.adgrowth.internal.integrations.admob.services.SendAdEventService
import com.adgrowth.internal.interfaces.integrations.AdViewIntegration
import com.google.android.gms.ads.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.adgrowth.internal.integrations.admob.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.admob.services.interfaces.SendAdEventService as ISendAdEventService


class AdMobAdView(
    private val manager: AdViewManager,
    private val getAdService: IGetAdService<AdView>,
    private val sendAdEventService: ISendAdEventService
) : AdViewIntegration(manager.context) {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mAdRequest: AdRequest.Builder
    private var mAd: AdView? = null
    private var mListener: Listener? = null
    private val adViewListener = object : AdListener() {
        override fun onAdClicked() {
            super.onAdClicked()
            sendAdEventService.run(AdEventType.CLICK)
            mListener?.onClicked()
        }

        override fun onAdFailedToLoad(e: LoadAdError) {
            super.onAdFailedToLoad(e)
            mListener?.onFailedToShow(e.message)
        }

        override fun onAdImpression() {
            super.onAdImpression()
            sendAdEventService.run(AdEventType.VIEW)
            mListener?.onImpression()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAd?.destroy()
    }

    override fun load(
        manager: AdViewManager
    ): AdViewIntegration {
        mListener = manager.listener

        mAdRequest = AdRequest.Builder()

        val profile = AdServer.clientProfile

        profile.interests.forEach { interest ->
            mAdRequest.addKeyword(interest)
        }
        layoutParams = LayoutHelper.getAdLayoutParams(manager.orientation, manager.size)
        mAd = getAdService.run(mAdRequest.build());
        mAd!!.adListener = adViewListener
        return this
    }

    override fun resumeAd() {
        mainScope.launch {
            mAd?.resume()
        }
    }

    override fun pauseAd() {
        mainScope.launch {
            mAd?.pause()
        }
    }

    override fun placeIn(parent: ViewGroup) {
        this.addView(mAd)
        if (parent.indexOfChild(this) >= 0) parent.removeView(this)
        parent.addView(this)
    }

    override fun setListener(listener: Listener) {
        mListener = listener
    }


    class Builder : AdViewManager.Builder {
        override fun build(manager: AdViewManager): AdViewIntegration {
            return AdMobAdView(
                manager, makeGetAdService(manager), makeSendAdEventService(manager)
            )
        }

        private fun makeGetAdService(manager: AdViewManager): IGetAdService<AdView> {
            return GetAdViewService(manager)
        }

        private fun makeSendAdEventService(manager: AdViewManager): ISendAdEventService {
            return SendAdEventService(manager)
        }
    }


}
