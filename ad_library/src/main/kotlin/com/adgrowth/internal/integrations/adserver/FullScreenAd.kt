package com.adgrowth.internal.integrations.adserver

import android.app.Activity
import android.app.Application
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.adgrowth.adserver.R
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.helpers.ScreenHelpers.getOrientation
import com.adgrowth.internal.integrations.adserver.http.AdRequest
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.enums.AdMediaType
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.adserver.helpers.ScreenHelpers.setOrientation
import com.adgrowth.internal.interfaces.integration.AdIntegration
import com.adgrowth.internal.integrations.adserver.views.AdDialog
import com.adgrowth.internal.integrations.adserver.views.AdImage
import com.adgrowth.internal.integrations.adserver.views.AdPlayer
import java.util.*

abstract class FullScreenAd<T : AdIntegration<T, Listener>, Listener : AdIntegration.Listener<T>> :
    AdIntegration<T, Listener>, AdImage.Listener {
    private var lastOrientation: AdOrientation? = null
    private lateinit var mAdContainerView: LinearLayout

    @JvmField
    protected var mDialog: AdDialog? = null

    @JvmField
    protected var mListener: Listener? = null

    @JvmField
    protected var mAdRequest: AdRequest? = null
    private var mRunningTimer: Timer? = Timer()
    private var mAdImage: AdImage? = null
    private var mFailedToLoad = false
    protected var ad: Ad? = null
    protected var mContext: Activity? = null
    protected var mCurrentRunningTime = 0
    protected var mAdDuration = Ad.DEFAULT_AD_DURATION
    protected var mVideoIsReady = false
    protected var mImageIsReady = false
    protected var mAdIsReady = false
    protected var mPlayer: AdPlayer? = null

    @Throws(AdRequestException::class)
    protected fun requestAd(context: Activity, adType: AdType) {
        mContext = context
        mFailedToLoad = false

        if (ad != null) {
            context.runOnUiThread { mListener!!.onFailedToLoad(AdRequestException(AdRequestException.ALREADY_LOADED)) }
            return
        }

        val options = HashMap<String, Any>()
        options["orientation"] = getOrientation(context)

        ad = mAdRequest!!.getAd(options)

        if (ad?.type !== adType) {
            throw AdRequestException(AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE)
        }

        val mediaType = ad?.mediaType

        if (mediaType === AdMediaType.VIDEO) {
            mPlayer = AdPlayer(
                context, ad?.mediaUrl, mPlayerListener
            )
            mPlayer!!.setOnClickListener(mOnAdClickListener)
        }

        val imageUrl = if (mediaType === AdMediaType.IMAGE) ad!!.mediaUrl
        else ad!!.postMediaUrl

        mAdImage = AdImage(context, imageUrl, this)
        mAdImage!!.setOnClickListener(mOnAdClickListener)
    }

    open fun show(context: Activity) {
        if (ad == null || !mAdIsReady) {
            mListener!!.onFailedToShow(Ad.NOT_READY)
            return
        }

        if (ad!!.isConsumed) {
            mListener!!.onFailedToShow(Ad.ALREADY_CONSUMED)
            return
        }

        if (!AdServerEventManager.showPermission) {
            mListener!!.onFailedToShow(Ad.ALREADY_SHOWING_FULL_SCREEN_AD)
            return
        }

        mContext = context
        val type = ad!!.mediaType

        prepareDialog()

        if (type === AdMediaType.IMAGE) mAdContainerView.addView(mAdImage)
        if (type === AdMediaType.VIDEO) mAdContainerView.addView(mPlayer)

        mDialog!!.show()
    }

    private fun prepareDialog() {
        mDialog = AdDialog(mContext!!)
        mDialog!!.setOnShowListener(mOnShowListener)
        mDialog!!.setOnDismissListener(mOnDismissListener)
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

    override fun isLoaded(): Boolean {
        return mAdIsReady
    }

    override fun isFailed(): Boolean {
        return mFailedToLoad
    }

    protected fun startRunningTimer() {
        if (mRunningTimer != null) mRunningTimer!!.cancel()

        mRunningTimer = Timer()

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                mCurrentRunningTime++
                mContext!!.runOnUiThread { onRunningTimeChanged(mCurrentRunningTime) }
            }
        }
        mRunningTimer!!.scheduleAtFixedRate(task, 1000, 1000)
    }

    protected fun pauseRunningTimer() {
        if (mRunningTimer != null) {
            mRunningTimer!!.cancel()
            mRunningTimer = null
        }
    }

    protected open fun onRunningTimeChanged(adStartedTime: Int) {
        if (adStartedTime >= TIME_TO_CLOSE && !mDialog!!.isCloseButtonEnabled) mDialog!!.enableCloseButton()
    }

    private val mOnAdClickListener = View.OnClickListener { view: View ->
        if (view.isEnabled) {
            mAdRequest!!.sendClick(mContext!!, ad!!)
            if (mListener != null) mListener!!.onClicked()
        }
    }
    private val onCloseListener = View.OnClickListener { dismiss() }
    private val mPlayerListener: AdPlayer.Listener = object : AdPlayer.Listener {
        override fun onVideoReady(videoDuration: Int) {
            mAdDuration = videoDuration
            mVideoIsReady = true
            if (mImageIsReady) mAdIsReady = true
            @Suppress("UNCHECKED_CAST")
            if (mAdIsReady) mListener!!.onLoad(this@FullScreenAd as T)
        }

        override fun onVideoFinished() {
            presentPostAd()
        }

        override fun onVideoError() {
            mListener!!.onFailedToLoad(AdRequestException(AdRequestException.PLAYBACK_ERROR))
        }

        override fun onVideoProgressChanged(position: Double, total: Double) {
            mDialog!!.setVideoProgress((position / total * 100).toInt())
        }
    }


    private val mOnShowListener =
        DialogInterface.OnShowListener { dialogInterface: DialogInterface? -> onShow(dialogInterface) }
    private val mActivityLifecycleListener: Application.ActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                startRunningTimer()
            }

            override fun onActivityResumed(activity: Activity) {
                startRunningTimer()
                if (mPlayer != null && ad!!.mediaType === AdMediaType.VIDEO) {
                    mPlayer!!.play()
                }
            }

            override fun onActivityPaused(activity: Activity) {
                pauseRunningTimer()
                if (mPlayer != null && ad!!.mediaType === AdMediaType.VIDEO) {
                    mPlayer!!.pause()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                pauseRunningTimer()
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                pauseRunningTimer()
            }
        }
    private val mOnDismissListener =
        DialogInterface.OnDismissListener {
            onDismiss()
        }

    override fun onImageReady() {
        mImageIsReady = true

        if (ad!!.mediaType === AdMediaType.VIDEO) {
            if (mVideoIsReady) mAdIsReady = true
        } else mAdIsReady = true

        if (mAdIsReady) mContext?.runOnUiThread {
            @Suppress("UNCHECKED_CAST")
            mListener?.onLoad(this@FullScreenAd as T)
        }
    }

    override fun onImageError() {
        mFailedToLoad = true
        mListener!!.onFailedToLoad(AdRequestException(AdRequestException.NETWORK_ERROR))
    }

    private fun onDismiss() {
        Objects.requireNonNull(mDialog!!.window)
            .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (lastOrientation != null)
            setOrientation(mContext!!, lastOrientation!!)

        mContext!!.application.unregisterActivityLifecycleCallbacks(mActivityLifecycleListener)
        mContext!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        mPlayer?.release()

        AdServerEventManager.notifyFullScreenDismissed()
        pauseRunningTimer()
    }

    protected open fun onShow(dialogInterface: DialogInterface?) {
        mContext!!.runOnUiThread { mListener!!.onImpression() }

        lastOrientation = getOrientation(mContext!!)

        setOrientation(mContext!!, ad!!.orientation)

        mContext!!.application.registerActivityLifecycleCallbacks(mActivityLifecycleListener)
        mContext!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        Objects.requireNonNull(mDialog!!.window)
            .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (mPlayer != null && ad!!.mediaType === AdMediaType.VIDEO) {
            mPlayer!!.play()
        }

        startRunningTimer()
        ad!!.isConsumed = true
        AdServerEventManager.notifyFullScreenShown(hashCode())
        mAdRequest!!.sendImpression(mContext!!, ad!!)
    }

    companion object {

        protected const val TIME_TO_CLOSE = 5
    }
}
