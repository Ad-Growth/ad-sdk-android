package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.interfaces.integrations.InterstitialIntegration


class InterstitialAd(unitId: String) : InterstitialIntegration.Listener {
    private lateinit var mContext: Activity
    private lateinit var mListener: Listener
    private var mAdManager: InterstitialManager

    init {
        mAdManager = InterstitialManager(unitId)
        mAdManager.listener = this
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun show(context: Activity) {
        mContext = context
        mAdManager.show(context)
    }

    fun load(context: Activity) {
        mContext = context
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

    override fun onLoad(ad: InterstitialIntegration) {
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

    interface Listener {
        fun onLoad(ad: InterstitialAd)
        fun onFailedToLoad(exception: AdRequestException?)

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
