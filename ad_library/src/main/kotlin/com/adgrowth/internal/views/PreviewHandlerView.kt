package com.adgrowth.internal.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.adgrowth.adserver.R
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize
import com.adgrowth.internal.helpers.LayoutHelper.Companion.getAdLayoutParams
import com.adgrowth.internal.integrations.InitializationManager


abstract class PreviewHandlerView : CenteredChildrenView {
    private var mPreviewText: TextView? = null
    protected lateinit var context: Activity
    var unitId: String = ""
    var size: AdSize = AdSize.BANNER
    var orientation: AdOrientation = AdOrientation.LANDSCAPE

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        getAdAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        getAdAttributes(context, attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        getAdAttributes(context, attrs)
    }

    internal abstract fun load()

    private fun getAdAttributes(context: Context, attrs: AttributeSet) {

        // this prevent preview problems on Android Studio
        val attributes = if (isInEditMode) {
            context.obtainStyledAttributes(attrs, R.styleable.AdView, 0, 0)
        } else (context as Activity).theme.obtainStyledAttributes(attrs, R.styleable.AdView, 0, 0)

        orientation = if (attributes.getInteger(
                R.styleable.AdView_orientation, 0
            ) == 1
        ) AdOrientation.PORTRAIT else AdOrientation.LANDSCAPE

        size = AdSize.values()[attributes.getInteger(R.styleable.AdView_size, 0)]
        unitId = attributes.getString(R.styleable.AdView_unit_id) as String


        if (isInEditMode) {
            setPreviewText("AdServer Banner", context)
            setBackgroundColor(resources.getColor(R.color.background_gray, null))
            attributes.recycle()

            if (unitId.isEmpty()) return setPreviewError( context)
            // stop here for avoid preview errors
            return
        }

        if (unitId.isEmpty())
            throw IllegalArgumentException("You must provide an unit_id for AdView")


        this.context = context as Activity

        if (InitializationManager.isInitialized) {
            load()
        }
    }

    private fun setPreviewError(context: Context) {
        setPreviewText("Missing unit_id", context)
        mPreviewText!!.background = resources.getDrawable(R.drawable.danger_border_square, null)
        mPreviewText!!.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun setPreviewText(text: String, context: Context) {
        if (mPreviewText == null) {
            mPreviewText = TextView(context)
            mPreviewText!!.layoutParams = getAdLayoutParams(orientation, size)
            mPreviewText!!.background = resources.getDrawable(R.drawable.border_square, null)
            mPreviewText!!.setTextColor(resources.getColor(R.color.black, null))
            mPreviewText!!.text = text
            mPreviewText!!.gravity = Gravity.CENTER
            addView(mPreviewText)
        }
        mPreviewText!!.text = text
    }
}
