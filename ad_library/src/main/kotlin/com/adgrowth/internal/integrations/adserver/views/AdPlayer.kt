package com.adgrowth.internal.integrations.adserver.views

import android.annotation.SuppressLint
import android.app.Activity
import android.webkit.JavascriptInterface
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.helpers.HTMLBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("ViewConstructor")
class AdPlayer(
    context: Activity,
    private val url: String,
    private val listener: Listener
) : WebViewMedia(context) {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    var adDuration = Ad.DEFAULT_AD_DURATION
        private set

    init {
        setupWebView(object {
            @JavascriptInterface
            fun onVideoReady(duration: Double) {
                mainScope.launch {
                    adDuration = duration
                    listener.onVideoReady(duration)
                }
            }

            @JavascriptInterface
            fun onClick() {
                mainScope.launch { listener.onClick() }
            }

            @JavascriptInterface
            fun onVideoError() {
                mainScope.launch { listener.onVideoError() }
            }

            @JavascriptInterface
            fun onPlay() {
                mainScope.launch { listener.onPlay() }
            }

            @JavascriptInterface
            fun onPause() {
                mainScope.launch { listener.onPause() }
            }

            @JavascriptInterface
            fun onVideoFinished() {
                mainScope.launch { listener.onVideoFinished() }
            }

            @JavascriptInterface
            fun onVideoProgressChanged(position: Double, total: Double) {
                mainScope.launch { listener.onVideoProgressChanged(position, total) }
            }
        })
    }


    override fun preload() {
        html = HTMLBuilder.getVideoHTML().replace("\\{media_url\\}".toRegex(), url)
        super.preload()
    }

    fun setMuted(muted: Boolean) {
        mWebView?.loadUrl("javascript:setMuted(${if (muted) "true" else "false"})")
    }

    fun play() {
        mWebView?.loadUrl("javascript:play()")
    }

    fun pause() {
        mWebView?.loadUrl("javascript:pause()")
    }

    interface Listener {
        fun onVideoProgressChanged(position: Double, total: Double)
        fun onVideoReady(videoDuration: Double)
        fun onPause() {}
        fun onPlay() {}
        fun onVideoFinished()
        fun onVideoError()
        fun onClick()
    }

}
