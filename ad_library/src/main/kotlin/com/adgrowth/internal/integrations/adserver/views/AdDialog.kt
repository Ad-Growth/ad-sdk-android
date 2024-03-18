package com.adgrowth.internal.integrations.adserver.views


import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.window.OnBackInvokedCallback

import com.adgrowth.adserver.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdDialog(private val context: Activity) : FrameLayout(context) {
    private var releasing: Boolean = false
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val mProgressBar: ProgressBar
    private val mCloseBtn: ImageView
    private val mCloseTextView: TextView
    private var mOriginalSystemUiVisibility = 0
    private var mOnDismissListener: OnDismissListener = object : OnDismissListener {
        override fun onDismiss() {}
    }
    private var mOnShowListener: OnShowListener = object : OnShowListener {
        override fun onShow() {}
    }

    init {

        val view = LayoutInflater.from(context).inflate(R.layout.activity_fit_content, this, true)

        view.findViewById<View>(R.id.modal_background)
            .setOnClickListener { /* just consume click */ }

        foregroundGravity = Gravity.CENTER

        mCloseTextView = findViewById<View>(R.id.close_text_view) as TextView
        mCloseBtn = findViewById<View>(R.id.close_btn) as ImageView
        mCloseBtn.isEnabled = false
        mProgressBar = findViewById<View>(R.id.video_progress) as ProgressBar
        mProgressBar.progress = 0
        mProgressBar.max = 100

    }

    fun setOnCloseListener(onCloseListener: OnClickListener?) {
        mCloseBtn.setOnClickListener(onCloseListener)
    }

    fun setVideoProgress(progress: Int) {
        mainScope.launch {
            if (mProgressBar.visibility != View.VISIBLE) mProgressBar.visibility = View.VISIBLE
            mProgressBar.progress = progress
        }
    }

    fun enableCloseButton() {
        mainScope.launch {
            mCloseBtn.isEnabled = true
            mCloseBtn.alpha = 1f
        }
    }

    val isCloseButtonEnabled: Boolean
        get() = mCloseBtn.isEnabled

    fun showButtonText() {
        mainScope.launch {
            mCloseTextView.visibility = View.VISIBLE
        }
    }

    fun hideButtonText() {
        mainScope.launch {
            mCloseTextView.visibility = View.GONE
        }
    }

    fun hideProgressBar() {
        mainScope.launch {
            mProgressBar.visibility = View.GONE
        }
    }

    fun setButtonLabelText(text: String?) {
        mainScope.launch {
            mCloseTextView.text = text
        }
    }

    fun show() {
        hideKeyboardAndClearFocus(context);
        mainScope.launch {
            attachToWindow()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mainScope.launch {
            val decorView = context.window.decorView
            mOriginalSystemUiVisibility = decorView.systemUiVisibility
            decorView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
            visibility = VISIBLE
            mOnShowListener.onShow()
        }
    }

    fun dismiss() {
        releasing = true
        mainScope.launch {
            val decorView = context.window.decorView
            decorView.systemUiVisibility = mOriginalSystemUiVisibility
            visibility = GONE

            if (parent != null) {
                (parent as ViewGroup).removeView(this@AdDialog)
            }
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (releasing)
            mainScope.launch {
                mOnDismissListener.onDismiss()
            }
    }

    private fun attachToWindow() {
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }

        if (context.window.decorView is ViewGroup) {
            val view = context.window.decorView as ViewGroup
            view.addView(this, layoutParams)
        } else
            context.window.addContentView(this, layoutParams)
    }

    fun setOnShowListener(listener: OnShowListener) {
        mOnShowListener = listener
    }

    fun setOnDismissListener(listener: OnDismissListener) {
        mOnDismissListener = listener
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    interface OnShowListener {
        fun onShow()
    }

    private fun hideKeyboardAndClearFocus(activity: Activity) {
        val focusedView = activity.currentFocus
        if (focusedView != null) {
            val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
            focusedView.clearFocus()
        }
    }
}
