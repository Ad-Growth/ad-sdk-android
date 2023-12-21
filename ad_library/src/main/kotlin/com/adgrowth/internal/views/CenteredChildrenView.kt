package com.adgrowth.internal.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize

abstract class CenteredChildrenView : ViewGroup {
    private var width: Int = 0
    private var height: Int = 0
    protected lateinit var mAdOrientation: AdOrientation
    protected lateinit var mAdSize: AdSize

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childLeft = (width - child.measuredWidth) / 2
            val childTop = (height - child.measuredHeight) / 2
            val childRight = childLeft + child.measuredWidth
            val childBottom = childTop + child.measuredHeight
            child.layout(childLeft, childTop, childRight, childBottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        width = MeasureSpec.getSize(widthMeasureSpec)
        height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height)

        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return true
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

}
