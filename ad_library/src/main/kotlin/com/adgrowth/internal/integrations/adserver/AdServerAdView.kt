package com.adgrowth.internal.integrations.adserver

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import com.adgrowth.adserver.R
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSizeType
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.registerFullScreenListener
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.unregisterFullScreenListener
import com.adgrowth.internal.integrations.adserver.http.AdRequest
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.enums.AdMediaType
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.interfaces.integration.AdViewIntegration
import com.adgrowth.internal.integrations.adserver.views.AdImage
import com.adgrowth.internal.integrations.adserver.views.AdPlayer
import com.adgrowth.internal.integrations.adserver.views.FillParentViewGroup
import java.util.*
import kotlin.collections.HashMap


class AdServerAdView : FillParentViewGroup,
    AdViewIntegration<AdServerAdView, AdViewIntegration.Listener<AdServerAdView>>,
    Application.ActivityLifecycleCallbacks, AdImage.Listener, AdPlayer.Listener,
    AdServerEventManager.FullScreenListener {

    private var mAdRequest: AdRequest? = null
    private var mContext: Activity? = null
    private var mImage: AdImage? = null
    private var mPlayer: AdPlayer? = null
    private var mListener: AdViewIntegration.Listener<AdServerAdView>? = null
    private var mAd: Ad? = null
    private var mFailedToLoad = false
    private var mAdIsReady = false
    private var mRunningTimer: Timer? = Timer()
    private var mCurrentRunningTime = 0
    private var mAdDuration: Int? = Ad.DEFAULT_AD_DURATION
    private var mOrientation: AdOrientation? = AdOrientation.LANDSCAPE
    private var mUnitId: String? = null
    private var mSize: AdSizeType = AdSizeType.BANNER

    private val onAdClickListener = OnClickListener {
        if (mAd == null) {
            mContext?.runOnUiThread { mListener?.onFailedToShow(Ad.NOT_READY) }
        } else {
            mAdRequest!!.sendClick(mContext!!, mAd!!)
            if (mListener != null) mContext!!.runOnUiThread { mListener!!.onClicked() }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pauseRunningTimer()
        unregisterFullScreenListener(this)
        mContext?.application?.unregisterActivityLifecycleCallbacks(this@AdServerAdView)
    }


    constructor(
        context: Activity, unitId: String, size: AdSizeType, orientation: AdOrientation
    ) : super(context) {
        mUnitId = unitId
        mSize = size
        mOrientation = orientation
        init()
    }

    constructor(context: Activity) : super(context) {
        mUnitId = ""
        init()
    }

    constructor(context: Activity, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        getAdAttributes(context, attrs)
        init()
    }

    constructor(
        context: Activity, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        getAdAttributes(context, attrs)
        init()
    }

    constructor(context: Activity, attrs: AttributeSet?) : super(context, attrs) {
        getAdAttributes(context, attrs)
        init()
    }

    override fun getSize(): AdSizeType {
        return mSize
    }


    override fun setListener(listener: AdViewIntegration.Listener<AdServerAdView>) {
        mListener = listener
    }

    private fun getAdAttributes(context: Activity, attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.AdView, 0, 0)

        mOrientation = if (attributes.getInteger(
                R.styleable.AdView_orientation, 0
            ) == 1
        ) AdOrientation.PORTRAIT else AdOrientation.LANDSCAPE

        mSize = AdSizeType.values()[attributes.getInteger(R.styleable.AdView_size, 0)]
        mUnitId = attributes.getString(R.styleable.AdView_unit_id)
    }

    private fun init() {
        if (mUnitId == null || mUnitId == "") {
            if (isInEditMode) showErrorOnPreview()
            else throw IllegalArgumentException("You must provide an unit_id for AdView")
        }

        // this prevent preview problems on android studio
        if (!isInEditMode) {
            mContext = context as Activity

            registerFullScreenListener(this)
            setOnClickListener(onAdClickListener)

            mContext!!.application.registerActivityLifecycleCallbacks(this@AdServerAdView)
            mAdRequest = AdRequest(mUnitId!!)

            loadAd()
        }
    }

    @SuppressLint("SetTextI18n")
    fun showErrorOnPreview() {
        setBackgroundColor(Color.parseColor("#FF0000"))
        val text = TextView(context)
        text.setTextColor(Color.parseColor("#FFFFFF"))
        text.text = "MISSING UNIT_ID"
        addView(text)
    }

    private fun loadAd() {
        Thread {
            mFailedToLoad = false
            mAdIsReady = false

            try {
                val options = HashMap<String, Any>()

                options["orientation"] = mOrientation.toString()
                options["dimension"] = mSize.toString()

                mAd = mAdRequest!!.getAd(options)

                if (mAd!!.type !== AdType.BANNER) throw AdRequestException(AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE)

                presentAd(mAd!!)
            } catch (e: AdRequestException) {

                // TODO: Check if is no-ads-error and get an ad from integrations

                mFailedToLoad = true

                mAdDuration =
                    if (e.code == AdRequestException.SDK_NOT_INITIALIZED) SDK_NOT_INITIALIZED_RETRY_TIME
                    else AFTER_ERROR_REFRESH_RATE


                startRunningTimer()
                if (mListener != null) mContext!!.runOnUiThread { mListener!!.onFailedToLoad(e) }
            }
        }.start()
    }

    private fun presentAd(ad: Ad) {
        mContext!!.runOnUiThread {
            mCurrentRunningTime = 0
            if (ad.mediaType === AdMediaType.IMAGE) {
                mImage = AdImage(
                    mContext, ad.mediaUrl, this
                )
                mImage!!.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
                )
                mImage!!.scaleType = ImageView.ScaleType.CENTER_CROP
                super.addView(mImage)
                return@runOnUiThread
            }
            mPlayer = AdPlayer(
                mContext, ad.mediaUrl, this
            )
            mPlayer!!.setScaleType(AdPlayer.ScaleType.FIT_CENTER)
            mPlayer!!.setMuted(true)
            super.addView(mPlayer)
        }
    }

    override fun reload() {
        refreshAd()
    }

    private fun refreshAd() {
        if (mAd != null) mAd = null
        pauseRunningTimer()
        mCurrentRunningTime = 0
        if (mImage != null) {
            removeView(mImage)
            mImage = null
        }
        if (mPlayer != null) {
            removeView(mPlayer)
            mPlayer!!.release()
            mPlayer = null
        }
        loadAd()
    }

    private fun onRunningTimeChanged(elapsedTime: Double) {
        println("CHANGED TIME: $elapsedTime")
        if (elapsedTime >= mAdDuration!!) {
            refreshAd()
        }
    }

    private fun startRunningTimer() {
        mRunningTimer?.cancel()

        if (mAdDuration == Ad.DISABLED_REFRESH_RATE) return

        mRunningTimer = Timer()

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                mCurrentRunningTime++
                mContext!!.runOnUiThread { onRunningTimeChanged(mCurrentRunningTime.toDouble()) }
            }
        }
        mRunningTimer!!.scheduleAtFixedRate(task, 1000, 1000)
    }

    private fun pauseRunningTimer() {
        if (mRunningTimer != null) {
            mRunningTimer!!.cancel()
            mRunningTimer = null
        }
    }

    private fun getAdDuration(refreshRate: Int?): Int? {
        if (refreshRate === Ad.AUTO_REFRESH_RATE) {
            if (mAd!!.mediaType === AdMediaType.VIDEO) {
                return mPlayer!!.adDuration
            }
            if (mAd!!.mediaType === AdMediaType.IMAGE) {
                return Ad.DEFAULT_AD_DURATION
            }
        }

        // 0 or 30-150
        return refreshRate
    }

    override fun onImageReady() {
        mAdIsReady = true
        mAdDuration = getAdDuration(mAd!!.refreshRate)
        if (mListener != null) {
            mContext!!.runOnUiThread {
                mListener!!.onLoad(this)
                mListener!!.onImpression()
            }
        }
        startRunningTimer()
        mAdRequest!!.sendImpression(mContext!!, mAd!!)
    }

    override fun onImageError() {
        mFailedToLoad = true
        mAdDuration = AFTER_ERROR_REFRESH_RATE
        startRunningTimer()
        if (mListener != null) mContext!!.runOnUiThread {
            mListener!!.onFailedToLoad(
                AdRequestException(AdRequestException.NETWORK_ERROR)
            )
        }
    }

    override fun isLoaded(): Boolean {
        return mAdIsReady
    }

    override fun isFailed(): Boolean {
        return mFailedToLoad
    }

    override fun getOrientation(): AdOrientation {
        return mOrientation!!
    }

    private fun pauseAd() {
        pauseRunningTimer()
        if (mAd!!.mediaType === AdMediaType.VIDEO) mPlayer?.pause()
    }

    private fun resumeAd() {
        startRunningTimer()
        if (mAd!!.mediaType === AdMediaType.VIDEO) mPlayer?.play()
    }

    override fun onVideoProgressChanged(position: Double, total: Double) {
        mCurrentRunningTime = position.toInt()
    }

    private fun isCurrentActivity(activity: Activity): Boolean {
        return mContext == activity
    }

    override fun onVideoReady(videoDuration: Int) {
        mAdDuration = getAdDuration(mAd!!.refreshRate)
        if (mListener != null) {
            mContext!!.runOnUiThread {
                mListener!!.onLoad(this)
                mListener!!.onImpression()
            }
        }
        mPlayer?.play()
        startRunningTimer()
        mAdRequest?.sendImpression(mContext!!, mAd!!)
    }

    override fun onVideoFinished() {
        refreshAd()
    }

    override fun onVideoError() {
        mListener?.onFailedToLoad(AdRequestException(AdRequestException.PLAYBACK_ERROR))
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if (isCurrentActivity(activity)) {
            resumeAd()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (isCurrentActivity(activity)) {
            pauseAd()
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (isCurrentActivity(activity)) {
            pauseRunningTimer()
            unregisterFullScreenListener(this)

            mContext?.application?.unregisterActivityLifecycleCallbacks(this@AdServerAdView)
            mPlayer?.release()
            mPlayer = null

        }
    }

    override fun onFullScreenShown(instanceHash: Int) {
        pauseAd()
    }

    override fun onFullScreenDismissed() {
        resumeAd()
    }


    interface Listener : AdViewIntegration.Listener<AdServerAdView?>
    companion object {
        private const val AFTER_ERROR_REFRESH_RATE = 10
        private const val SDK_NOT_INITIALIZED_RETRY_TIME = 3
    }
}
