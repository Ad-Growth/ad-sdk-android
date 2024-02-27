package com.adgrowth.internal.integrations.adserver.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

class AdDialog(context: Context) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {
    private val mProgressBar: ProgressBar
    private val mCloseBtn: ImageView
    private val mCloseTextView: TextView

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(com.adgrowth.adserver.R.layout.activity_fit_content)
        setCancelable(false)
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mCloseTextView = findViewById<View>(com.adgrowth.adserver.R.id.close_text_view) as TextView
        mCloseBtn = findViewById<View>(com.adgrowth.adserver.R.id.close_btn) as ImageView
        mCloseBtn.isEnabled = false
        mProgressBar = findViewById<View>(com.adgrowth.adserver.R.id.video_progress) as ProgressBar
        mProgressBar.progress = 0
        mProgressBar.max = 100
    }

    fun setOnCloseListener(onCloseListener: View.OnClickListener?) {
        mCloseBtn.setOnClickListener(onCloseListener)
    }

    fun setVideoProgress(progress: Int) {
        mProgressBar.post {
            if (mProgressBar.visibility != View.VISIBLE) mProgressBar.visibility = View.VISIBLE
            mProgressBar.progress = progress
        }
    }

    fun enableCloseButton() {
        mCloseBtn.post {
            mCloseBtn.isEnabled = true
            mCloseBtn.alpha = 1f
        }
    }

    val isCloseButtonEnabled: Boolean
        get() = mCloseBtn.isEnabled

    fun showButtonText() {
        mCloseTextView.post {
            mCloseTextView.visibility = View.VISIBLE
        }
    }

    fun hideButtonText() {
        mCloseTextView.post {
            mCloseTextView.visibility = View.GONE
        }
    }

    fun hideProgressBar() {
        mProgressBar.post {
            mProgressBar.visibility = View.GONE
        }
    }

    fun setButtonLabelText(text: String?) {
        mCloseTextView.post {
            mCloseTextView.text = text
        }
    }
}
