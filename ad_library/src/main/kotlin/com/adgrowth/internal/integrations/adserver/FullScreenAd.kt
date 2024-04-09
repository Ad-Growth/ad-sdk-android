package com.adgrowth.internal.integrations.adserver

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.R
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.adserver.helpers.LayoutHelpers
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.enums.AdMediaType
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService
import com.adgrowth.internal.integrations.adserver.views.AdDialog
import com.adgrowth.internal.integrations.adserver.views.AdImage
import com.adgrowth.internal.integrations.adserver.views.AdPlayer
import com.adgrowth.internal.interfaces.integrations.AdIntegration
import com.adgrowth.internal.interfaces.listeners.AdListener
import com.adgrowth.internal.interfaces.managers.AdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class FullScreenAd<T : AdIntegration<T, Listener>, Listener : AdListener<T>>(
    private val sendAdEventService: SendAdEventService
) : AdIntegration<T, Listener>, AdDialog.OnShowListener, AdDialog.OnDismissListener {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)
    val instanceId = hashCode()
    protected var mListener: Listener? = null
    protected lateinit var mContext: Activity

    var mFailedToLoad = false

    protected var mDialog: AdDialog? = null
    private var lastRotation: Int? = null
    private var mRunningTimer: Timer? = Timer()

    protected lateinit var mAd: Ad

    protected var mCurrentRunningTime = 0
    protected var mAdDuration: Double = Ad.DEFAULT_AD_DURATION
    protected var mVideoIsReady = false
    protected var mImageIsReady = false
    protected var mAdIsReady = false
    protected lateinit var mLoadFuture: CompletableFuture<T>
    private lateinit var mAdContainerView: LinearLayout
    protected var mAdImage: AdImage? = null
    protected var mAdPlayer: AdPlayer? = null

    open fun prepareAdMedia(ad: Ad) {

        val mediaType = ad.mediaType

        if (mediaType === AdMediaType.VIDEO) {
            mAdPlayer = AdPlayer(
                mContext, ad.mediaUrl, mPlayerListener
            )
        }

        val imageUrl = if (mediaType === AdMediaType.IMAGE) ad.mediaUrl
        else ad.postMediaUrl

        mAdImage = AdImage(mContext, imageUrl, mImageListener)

    }


    open fun show(manager: AdManager<*, *>) {
        if (!mAdIsReady) {
            mListener?.onFailedToShow(AdRequestException.NOT_READY)
            return
        }

        if (mAd.isConsumed) {
            mListener?.onFailedToShow(AdRequestException.ALREADY_CONSUMED)
            return
        }

        mContext = manager.context
        val mediaType = mAd.mediaType

        prepareDialog()

        if (mediaType === AdMediaType.IMAGE) mAdImage?.addInto(mAdContainerView)
        if (mediaType === AdMediaType.VIDEO) mAdPlayer?.addInto(mAdContainerView)

        AdServerEventManager.notifyFullScreenShown(manager.hashCode())

        mDialog?.show()
    }

    private fun prepareDialog() {
        mDialog = AdDialog(mContext)
        mDialog!!.setOnShowListener(this)
        mDialog!!.setOnDismissListener(this)
        mDialog!!.setOnCloseListener(onCloseListener)
        mAdContainerView = mDialog!!.findViewById(R.id.content_container)
    }

    protected fun presentPostAd() {
        mAdPlayer!!.release()
        mAdImage!!.addInto(mAdContainerView)

        mDialog!!.hideProgressBar()

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
                mainScope.launch { onRunningTimeChanged(mCurrentRunningTime) }
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
        if (ad.type !== type) throw AdRequestException(
            AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE
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

    protected val mOnAdClickListener: () -> Unit = {
        if (mAdContainerView.isEnabled) {
            sendAdEventService.run(AdEventType.CLICK, mAd)
            if (mListener != null) mListener!!.onClicked()
        }
    }
    private val onCloseListener = View.OnClickListener { dismiss() }
    private val mPlayerListener: AdPlayer.Listener = object : AdPlayer.Listener {
        override fun onVideoReady(videoDuration: Double) {
            mAdDuration = videoDuration
            mVideoIsReady = true
            if (mImageIsReady) mAdIsReady = true
            if (mAdIsReady) {
                @Suppress("UNCHECKED_CAST") mLoadFuture.complete(this@FullScreenAd as T)
            }
        }

        override fun onVideoFinished() {
            mainScope.launch {
                presentPostAd()
            }
        }

        override fun onVideoError() {
            mLoadFuture.completeExceptionally(
                APIIOException(
                    HTTPStatusCode.NO_RESPONSE, AdRequestException.PLAYBACK_ERROR
                )
            )
        }

        override fun onClick() {
            mOnAdClickListener.invoke()
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
                if (mAdPlayer != null && mAd.mediaType === AdMediaType.VIDEO) {
                    mAdPlayer!!.play()
                }
            }

            override fun onActivityPaused(activity: Activity) {
                stopRunningTimer()
                if (mAdPlayer != null && mAd.mediaType === AdMediaType.VIDEO) {
                    mAdPlayer!!.pause()
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
    private val mImageListener: AdImage.Listener = object : AdImage.Listener {
        override fun onClick() {
            mAdImage.let {
                mOnAdClickListener.invoke()
            }
        }

        override fun onImageReady() {
            mImageIsReady = true

            if (mAd.mediaType === AdMediaType.VIDEO) {
                if (mVideoIsReady) mAdIsReady = true
            } else mAdIsReady = true

            if (mAdIsReady) mainScope.launch {
                @Suppress("UNCHECKED_CAST") mLoadFuture.complete(this@FullScreenAd as T)
            }
        }

        override fun onImageError() {
            mFailedToLoad = true
            mLoadFuture.completeExceptionally(
                APIIOException(
                    403, AdRequestException.PLAYBACK_ERROR
                )
            )
        }
    }

    override fun onDismiss() {
        mContext.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (lastRotation != null) LayoutHelpers.setScreenRotation(mContext, null)

        mContext.application.unregisterActivityLifecycleCallbacks(mActivityLifecycleListener)
        mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        mAdPlayer?.release()
        mAdImage?.release()

        AdServerEventManager.notifyFullScreenDismissed()
        stopRunningTimer()


        mainScope.launch {
            mListener!!.onDismissed()
        }
    }

    override fun onShow() {
        mainScope.launch { mListener!!.onImpression() }

        lastRotation = LayoutHelpers.getScreenRotation(mContext)

        LayoutHelpers.setScreenRotation(mContext, mAd.orientation)

        mContext.application.registerActivityLifecycleCallbacks(mActivityLifecycleListener)
        mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        mContext.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (mAdPlayer != null && mAd.mediaType === AdMediaType.VIDEO) {
            mAdPlayer!!.play()
        }

        startRunningTimer()
        mAd.isConsumed = true

        sendAdEventService.run(AdEventType.VIEW, mAd)
    }

    companion object {
        protected const val TIME_TO_CLOSE = 5
    }
}
