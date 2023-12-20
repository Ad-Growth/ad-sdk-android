package com.adgrowth.internal.integrations.adserver

import android.app.Activity
import android.app.Application
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.R
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.helpers.ScreenHelpers.getOrientation
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.adserver.enums.AdMediaType
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.adserver.helpers.ScreenHelpers.setOrientation
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService
import com.adgrowth.internal.interfaces.integrations.AdIntegration
import com.adgrowth.internal.integrations.adserver.views.AdDialog
import com.adgrowth.internal.integrations.adserver.views.AdImage
import com.adgrowth.internal.integrations.adserver.views.AdPlayer
import com.adgrowth.internal.interfaces.managers.AdManager
import com.adgrowth.internal.interfaces.listeners.AdListener
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class FullScreenAd<T : AdIntegration<T, Listener>, Listener : AdListener<T>>(
    private val sendAdEventService: SendAdEventService
) : AdIntegration<T, Listener>, AdImage.Listener, DialogInterface.OnShowListener,
    DialogInterface.OnDismissListener {
    protected var mListener: Listener? = null
    protected lateinit var mContext: Activity

    var mFailedToLoad = false

    protected var mDialog: AdDialog? = null

    private var lastOrientation: AdOrientation? = null
    private var mRunningTimer: Timer? = Timer()

    protected lateinit var mAd: Ad

    protected var mCurrentRunningTime = 0
    protected var mAdDuration = Ad.DEFAULT_AD_DURATION
    protected var mVideoIsReady = false
    protected var mImageIsReady = false
    protected var mAdIsReady = false
    protected lateinit var mLoadFuture: CompletableFuture<T>
    private lateinit var mAdContainerView: LinearLayout
    private var mAdImage: AdImage? = null
    protected var mPlayer: AdPlayer? = null

    open fun prepareAdMedia(ad: Ad) {

        val mediaType = ad.mediaType

        if (mediaType === AdMediaType.VIDEO) {
            mPlayer = AdPlayer(
                mContext, ad.mediaUrl, mPlayerListener
            )
            mPlayer!!.setOnClickListener(mOnAdClickListener)
        }

        val imageUrl = if (mediaType === AdMediaType.IMAGE) ad.mediaUrl
        else ad.postMediaUrl

        mAdImage = AdImage(mContext, imageUrl, this)
        mAdImage!!.setOnClickListener(mOnAdClickListener)
    }


    open fun show(manager: AdManager<*, *>) {
        if (!mAdIsReady) {
            mListener!!.onFailedToShow(AdRequestException.NOT_READY)
            return
        }

        if (mAd.isConsumed) {
            mListener!!.onFailedToShow(AdRequestException.ALREADY_CONSUMED)
            return
        }

        mContext = manager.context
        val type = mAd.mediaType

        prepareDialog()

        if (type === AdMediaType.IMAGE) mAdContainerView.addView(mAdImage)
        if (type === AdMediaType.VIDEO) mAdContainerView.addView(mPlayer)

        mDialog?.show()
    }

    private fun prepareDialog() {
        mDialog = AdDialog(mContext)
        mDialog!!.setOnShowListener(this)
        mDialog!!.setOnDismissListener(this)
        mDialog!!.setOnCloseListener(onCloseListener)
        mAdContainerView = mDialog!!.findViewById(R.id.content_container)
        mAdContainerView.setOnClickListener(mOnAdClickListener)
    }

    protected fun presentPostAd() {
        mPlayer!!.visibility = View.GONE
        mPlayer!!.release()
        mDialog!!.hideProgressBar()
        mAdContainerView.addView(mAdImage)
        mAdContainerView.removeView(mPlayer)
        mAdImage!!.visibility = View.VISIBLE
    }

    protected open fun dismiss() {
        mDialog!!.dismiss()
    }

    protected fun startRunningTimer() {
        if (mRunningTimer != null) mRunningTimer!!.cancel()

        mRunningTimer = Timer()

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                mCurrentRunningTime++
                mContext.runOnUiThread { onRunningTimeChanged(mCurrentRunningTime) }
            }
        }
        mRunningTimer!!.scheduleAtFixedRate(task, 1000, 1000)
    }

    protected fun beforeLoadCheck(): Boolean {
        if (!AdServer.isInitialized) throw APIIOException(
            HTTPStatusCode.NO_RESPONSE, AdRequestException.SDK_NOT_INITIALIZED
        )

        if (mAdIsReady) throw APIIOException(403, AdRequestException.ALREADY_LOADED)

        return true
    }

    protected fun afterLoadCheck(ad: Ad, type: AdType): Boolean {
        if (ad.type !== type) throw APIIOException(
            403, AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE
        )
        return true
    }

    protected fun stopRunningTimer() {
        if (mRunningTimer != null) {
            mRunningTimer!!.cancel()
            mRunningTimer = null
        }
    }

    protected open fun onRunningTimeChanged(elapsedTime: Int) {
        if (elapsedTime >= TIME_TO_CLOSE && !mDialog!!.isCloseButtonEnabled) mDialog!!.enableCloseButton()
    }

    private val mOnAdClickListener = View.OnClickListener { view: View ->
        if (view.isEnabled) {
            Thread {
                sendAdEventService.run(AdEventType.CLICK, mAd)
            }.start()
            if (mListener != null) mListener!!.onClicked()
        }
    }
    private val onCloseListener = View.OnClickListener { dismiss() }
    private val mPlayerListener: AdPlayer.Listener = object : AdPlayer.Listener {
        override fun onVideoReady(videoDuration: Int) {
            mAdDuration = videoDuration
            mVideoIsReady = true
            if (mImageIsReady) mAdIsReady = true
            if (mAdIsReady) {
                @Suppress("UNCHECKED_CAST")
                //
                mLoadFuture.complete(this@FullScreenAd as T)
            }
        }

        override fun onVideoFinished() {
            presentPostAd()
        }

        override fun onVideoError() {
            mLoadFuture.completeExceptionally(
                APIIOException(
                    HTTPStatusCode.NO_RESPONSE,
                    AdRequestException.PLAYBACK_ERROR
                )
            )
        }

        override fun onVideoProgressChanged(position: Double, total: Double) {
            mDialog?.setVideoProgress((position / total * 100).toInt())
        }
    }


    private val mActivityLifecycleListener: Application.ActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                startRunningTimer()
            }

            override fun onActivityResumed(activity: Activity) {
                startRunningTimer()
                if (mPlayer != null && mAd.mediaType === AdMediaType.VIDEO) {
                    mPlayer!!.play()
                }
            }

            override fun onActivityPaused(activity: Activity) {
                stopRunningTimer()
                if (mPlayer != null && mAd.mediaType === AdMediaType.VIDEO) {
                    mPlayer!!.pause()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                stopRunningTimer()
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                stopRunningTimer()
            }
        }


    override fun onImageReady() {
        mImageIsReady = true

        if (mAd.mediaType === AdMediaType.VIDEO) {
            if (mVideoIsReady) mAdIsReady = true
        } else mAdIsReady = true

        if (mAdIsReady) mContext.runOnUiThread {
            @Suppress("UNCHECKED_CAST")
            //
            mLoadFuture.complete(this@FullScreenAd as T)
        }
    }

    override fun onImageError() {
        mFailedToLoad = true
        mLoadFuture.completeExceptionally(APIIOException(403, AdRequestException.PLAYBACK_ERROR))
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        mDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (lastOrientation != null) setOrientation(mContext, lastOrientation!!)

        mContext.application.unregisterActivityLifecycleCallbacks(mActivityLifecycleListener)
        mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        mPlayer?.release()

        AdServerEventManager.notifyFullScreenDismissed()
        stopRunningTimer()
    }

    override fun onShow(dialogInterface: DialogInterface?) {
        mContext.runOnUiThread { mListener!!.onImpression() }

        lastOrientation = getOrientation(mContext)

        setOrientation(mContext, mAd.orientation)

        mContext.application.registerActivityLifecycleCallbacks(mActivityLifecycleListener)
        mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        mDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (mPlayer != null && mAd.mediaType === AdMediaType.VIDEO) {
            mPlayer!!.play()
        }

        startRunningTimer()
        mAd.isConsumed = true
        AdServerEventManager.notifyFullScreenShown(hashCode())
        Thread {
            sendAdEventService.run(AdEventType.VIEW, mAd)
        }.start()
    }

    companion object {
        protected const val TIME_TO_CLOSE = 5
    }
}
