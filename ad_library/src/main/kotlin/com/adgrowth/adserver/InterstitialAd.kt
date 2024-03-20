package com.adgrowth.adserver

import android.app.Activity
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.interfaces.integrations.InterstitialIntegration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InterstitialAd(unitId: String) : InterstitialIntegration.Listener {
    private val mainScope = CoroutineScope(Dispatchers.Main)
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
        if (!mAdManager.isLoaded) {
            mListener.onFailedToShow(AdRequestException.NOT_READY)
            return
        }
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
        mainScope.launch { mListener.onDismissed() }
    }

    override fun onLoad(ad: InterstitialIntegration) {
        mainScope.launch { mListener.onLoad(this@InterstitialAd) }
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        mainScope.launch { mListener.onFailedToLoad(exception) }
    }

    override fun onClicked() {
        mainScope.launch { mListener.onClicked() }
    }

    override fun onFailedToShow(code: String?) {
        mainScope.launch { mListener.onFailedToShow(code) }
    }

    override fun onImpression() {
        mainScope.launch { mListener.onImpression() }
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
