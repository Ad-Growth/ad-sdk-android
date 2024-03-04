package com.adgrowth.internal.integrations.adserver.views

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import java.lang.Exception

abstract class WebViewMedia(
    private val context: Activity,
    private val url: String,
) {
    protected lateinit var html: String
    protected var mWebView: WebView? = null
    protected val mMainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    protected fun setupWebView(javascriptInterface: Any) {
        context.runOnUiThread {
            mWebView = WebView(context)

            mWebView!!.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            mWebView?.apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                addJavascriptInterface(javascriptInterface, JAVASCRIPT_INTERFACE_OBJECT)
                preload()
            }
        }
    }

    protected open fun preload() {
        mWebView?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }


    fun addInto(container: ViewGroup) {
        mWebView?.let { webView ->
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }

            if (webView.parent != null) (webView.parent as ViewGroup).removeView(webView)

            container.addView(webView)
        }
    }

    fun removeFrom(layout: ViewGroup) {
        mWebView?.let { webView ->
            layout.removeView(webView)
        }
    }

    fun release() {
        mWebView?.let {
            try {
                it.stopLoading()
                it.removeAllViews()
                it.destroy()
            } catch (_: Exception) {

            }
        }
    }

    companion object {
        private const val JAVASCRIPT_INTERFACE_OBJECT = "ads"
    }

}