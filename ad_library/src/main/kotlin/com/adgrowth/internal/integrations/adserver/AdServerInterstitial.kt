package com.adgrowth.internal.integrations.adserver

import android.app.Activity
import com.adgrowth.adserver.BuildConfig
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.http.AdRequest
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.interfaces.integration.InterstitialIntegration

class AdServerInterstitial(unitId: String) :
    FullScreenAd<AdServerInterstitial, AdServerInterstitial.Listener>(),
    InterstitialIntegration<AdServerInterstitial, AdServerInterstitial.Listener> {
    init {
        mAdRequest = AdRequest(unitId)
    }


    override fun load(context: Activity) {
        Thread {
            try {
                super.requestAd(context, AdType.INTERSTITIAL)
            } catch (e: AdRequestException) {

                // TODO: Check if is no-ads-error and get an ad from integrations

                context.runOnUiThread { mListener!!.onFailedToLoad(e) }
            }
        }.start()
    }

    override fun dismiss() {
        mContext?.runOnUiThread { mListener!!.onDismissed() }
        super.dismiss()
    }

    override fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener : InterstitialIntegration.Listener<AdServerInterstitial>
}
