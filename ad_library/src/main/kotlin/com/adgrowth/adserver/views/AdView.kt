package com.adgrowth.adserver.views

import android.app.Activity
import android.util.AttributeSet

import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSizeType
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.AdServerAdView
import com.adgrowth.internal.integrations.adserver.views.FillParentViewGroup
import com.adgrowth.internal.interfaces.integration.AdViewIntegration

class AdView : FillParentViewGroup, AdViewIntegration<AdView, AdView.Listener>,
    AdViewIntegration.Listener<AdServerAdView> {
    private val mAd: AdServerAdView
    private var listener: Listener? = null

    constructor(context: Activity) : super(context) {
        mAd = AdServerAdView(context)
        mAd.setListener(this)
        addView(mAd)
    }

    constructor(context: Activity, attrs: AttributeSet?) : super(context, attrs) {
        mAd = AdServerAdView(context, attrs)
        mAd.setListener(this)
        addView(mAd)
    }

    constructor(context: Activity, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        mAd = AdServerAdView(context, attrs, defStyleAttr)
        mAd.setListener(this)
        addView(mAd)
    }

    constructor(
        context: Activity, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        mAd = AdServerAdView(context, attrs, defStyleAttr, defStyleRes)
        mAd.setListener(this)
        addView(mAd)
    }

    constructor(
        context: Activity, unitId: String, size: AdSizeType, orientation: AdOrientation
    ) : super(context) {
        mAd = AdServerAdView(context, unitId, size, orientation)
        mAd.setListener(this)
        addView(mAd)
    }


    interface Listener : AdViewIntegration.Listener<AdView>

    override fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun getSize(): AdSizeType {
        return mAd.getSize()
    }

    override fun getOrientation(): AdOrientation {
        return mAd.getOrientation()
    }

    override fun reload() {
        mAd.reload()
    }

    override fun isLoaded(): Boolean {
        return mAd.isLoaded()
    }

    override fun isFailed(): Boolean {
        return mAd.isFailed()
    }

    override fun onLoad(ad: AdServerAdView) {
        listener?.onLoad(this)
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        listener?.onFailedToLoad(exception)
    }

    override fun onClicked() {
        listener?.onClicked()
    }

    override fun onFailedToShow(code: String?) {
        listener?.onFailedToShow(code)
    }

    override fun onImpression() {
        listener?.onImpression()
    }


}
