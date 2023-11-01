package com.adgrowth.adserver.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSizeType
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.AdServerAdView
import com.adgrowth.internal.interfaces.integration.AdViewIntegration

class AdView : ViewGroup, AdViewIntegration<AdView, AdView.Listener>,
    AdViewIntegration.Listener<AdServerAdView> {
    private val mAd: AdServerAdView
    private lateinit var listener: Listener

    constructor(context: Context) : super(context) {
        mAd = AdServerAdView(context)
        addView(mAd)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mAd = AdServerAdView(context, attrs)
        addView(mAd)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        mAd = AdServerAdView(context, attrs, defStyleAttr)
        addView(mAd)
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        mAd = AdServerAdView(context, attrs, defStyleAttr, defStyleRes)
        addView(mAd)
    }

    constructor(
        context: Context, unitId: String, size: AdSizeType, orientation: AdOrientation
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

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val childCount = childCount
        var x = 0
        var y = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            x += child.measuredWidth
            if (i % 2 != 0) {
                x = 0
                y += child.measuredHeight
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(widthSize, heightSize)

        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)

        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return true
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun onLoad(ad: AdServerAdView) {
        listener.onLoad(this)
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        listener.onFailedToLoad(exception)
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


}
