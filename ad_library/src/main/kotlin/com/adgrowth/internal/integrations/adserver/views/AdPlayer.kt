package com.adgrowth.internal.integrations.adserver.views

import android.annotation.SuppressLint
import android.app.Activity
import android.webkit.JavascriptInterface
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.helpers.HTMLBuilder
import java.util.*

@SuppressLint("ViewConstructor")
class AdPlayer(
    context: Activity,
    private val url: String,
    private val listener: Listener
) : WebViewMedia(context) {

    var adDuration = Ad.DEFAULT_AD_DURATION
        private set

    init {
        setupWebView(object {
            @JavascriptInterface
            fun onVideoReady(duration: Double) {
                mMainHandler.post {
                    adDuration = duration
                    listener.onVideoReady(duration)
                }
            }

            @JavascriptInterface
            fun onClick() {
                mMainHandler.post { listener.onClick() }
            }

            @JavascriptInterface
            fun onVideoError() {
                mMainHandler.post { listener.onVideoError() }
            }

            @JavascriptInterface
            fun onPlay() {
                mMainHandler.post { listener.onPlay() }
            }

            @JavascriptInterface
            fun onPause() {
                mMainHandler.post { listener.onPause() }
            }

            @JavascriptInterface
            fun onVideoFinished() {
                mMainHandler.post { listener.onVideoFinished() }
            }

            @JavascriptInterface
            fun onVideoProgressChanged(position: Double, total: Double) {
                 mMainHandler.post { listener.onVideoProgressChanged(position, total) }
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
