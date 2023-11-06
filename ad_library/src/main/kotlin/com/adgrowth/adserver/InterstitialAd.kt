package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.AdServerInterstitial
import com.adgrowth.internal.interfaces.integration.InterstitialIntegration


class InterstitialAd(unitId: String) :    AdServerInterstitial.Listener {
    private lateinit var listener: Listener
    private var mAd: AdServerInterstitial

    init {
        mAd = AdServerInterstitial(unitId)
        mAd.setListener(this)
    }

    fun setListener(listener: Listener) {
        this.listener = listener;
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
        listener?.onDismissed()
    }

    override fun onLoad(ad: AdServerInterstitial) {
        listener.onLoad(this)
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        listener?.onFailedToLoad(exception)
    }

    override fun onClicked() {
        listener.onClicked()
    }

    override fun onFailedToShow(code: String?) {
        listener.onFailedToShow(code)
    }

    override fun onImpression() {
        listener.onImpression()
    }

    interface Listener : InterstitialIntegration.Listener<InterstitialAd>

}
