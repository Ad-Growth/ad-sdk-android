package com.adgrowth.internal.integrations.adserver.views

import android.app.Activity
import android.webkit.JavascriptInterface
import com.adgrowth.internal.integrations.adserver.helpers.HTMLBuilder

class AdImage(
    context: Activity,
    private val url: String,
    private val listener: Listener
) : WebViewMedia(context) {

    init {
        setupWebView(object {
            @JavascriptInterface
            fun onImageReady() {
                mMainHandler.post { listener.onImageReady() }
            }

            @JavascriptInterface
            fun onClick() {
                mMainHandler.post { listener.onClick() }
            }

            @JavascriptInterface
            fun onImageError() {
                mMainHandler.post { listener.onImageError() }
            }
        })
    }


    override fun preload() {
        html = HTMLBuilder.getImageHTML().replace("\\{media_url\\}".toRegex(), url)
        super.preload()
    }

    interface Listener {
        fun onClick()
        fun onImageReady()
        fun onImageError()
    }
}