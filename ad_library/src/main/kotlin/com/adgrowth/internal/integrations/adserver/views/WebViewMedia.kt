package com.adgrowth.internal.integrations.adserver.views

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class WebViewMedia(
    private val context: Activity,
) {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var _parent: ViewGroup? = null
    protected lateinit var html: String
    protected var mWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    protected fun setupWebView(javascriptInterface: Any) {
        mainScope.launch {
            mWebView = WebView(context)

            mWebView!!.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            mWebView?.apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.domStorageEnabled = false
                webViewClient = WebViewClient()
                setBackgroundColor(Color.TRANSPARENT)
                addJavascriptInterface(javascriptInterface, JAVASCRIPT_INTERFACE_OBJECT)
                preload()

            }
        }
    }

    protected open fun preload() {
        mWebView?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }


    fun addInto(container: ViewGroup) {
        _parent = container
        mWebView?.let { webView ->
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }

            if (webView.parent != null) (webView.parent as ViewGroup).removeView(webView)

            _parent!!.addView(webView)
        }
    }


    fun release() {
        mWebView?.let {
            try {
                _parent?.removeView(it)
                _parent = null
                it.stopLoading()
                it.removeAllViews()
                it.destroy()
                mWebView = null
            } catch (_: Exception) {

            }

        }
    }

    companion object {
        private const val JAVASCRIPT_INTERFACE_OBJECT = "ads"
    }

}