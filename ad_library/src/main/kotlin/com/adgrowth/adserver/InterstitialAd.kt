package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.AdServerInterstitial
import com.adgrowth.internal.interfaces.integration.InterstitialIntegration


class InterstitialAd(unitId: String) :    AdServerInterstitial.Listener {
    private lateinit var mListener: Listener
    private var mAd: AdServerInterstitial

    init {
        mAd = AdServerInterstitial(unitId)
        mAd.setListener(this)
    }

    fun setListener(listener: Listener) {
        this.mListener = listener;
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

    override fun onLoad(ad: AdServerInterstitial) {
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

    interface Listener : InterstitialIntegration.Listener<InterstitialAd>

}
