package com.adgrowth.internal.integrations.adserver.views

import android.app.Activity
import android.webkit.JavascriptInterface
import com.adgrowth.internal.integrations.adserver.helpers.HTMLBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdImage(
    context: Activity,
    private val url: String,
    private val listener: Listener
) : WebViewMedia(context) {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        setupWebView(object {
            @JavascriptInterface
            fun onImageReady() {
                mainScope.launch { listener.onImageReady() }
            }

            @JavascriptInterface
            fun onClick() {
                mainScope.launch { listener.onClick() }
            }

            @JavascriptInterface
            fun onImageError() {
                mainScope.launch { listener.onImageError() }
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