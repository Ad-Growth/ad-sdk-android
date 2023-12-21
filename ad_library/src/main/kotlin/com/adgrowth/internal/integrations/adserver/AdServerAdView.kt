package com.adgrowth.internal.integrations.adserver

import android.annotation.SuppressLint
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.helpers.LayoutHelper.Companion.getAdLayoutParams
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.integrations.AdViewManager
import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.integrations.adserver.enums.AdMediaType
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager.showPermission
import com.adgrowth.internal.integrations.adserver.services.GetAdService
import com.adgrowth.internal.integrations.adserver.services.SendAdEventService
import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAdService as IGetAdService
import com.adgrowth.internal.integrations.adserver.services.interfaces.SendAdEventService as ISendAdEventService
import com.adgrowth.internal.integrations.adserver.views.AdImage
import com.adgrowth.internal.integrations.adserver.views.AdPlayer
import com.adgrowth.internal.interfaces.integrations.AdViewIntegration
import java.util.*
import java.util.concurrent.CompletableFuture

@SuppressLint("ViewConstructor")
class AdServerAdView(
    private val manager: AdViewManager,
    private val getAdService: IGetAdService,
    private val sendAdEventService: ISendAdEventService
) : AdViewIntegration(manager.context), AdImage.Listener, AdPlayer.Listener {
    private lateinit var mLoadFuture: CompletableFuture<AdViewIntegration>
    private var mAd: Ad? = null
    private val mContext = manager.context
    private var mImage: AdImage? = null
    private var mPlayer: AdPlayer? = null
    private var mListener: AdViewIntegration.Listener? = null
    private var mRunningTimer: Timer? = Timer()
    private var mCurrentRunningTime = 0
    private var mAdDuration: Int? = Ad.DEFAULT_AD_DURATION

    private val onAdClickListener = OnClickListener {
        sendAdEventService.run(AdEventType.CLICK, mAd!!)
        if (mListener != null) mListener!!.onClicked()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRunningTimer()
    }

    init {
        setOnClickListener(onAdClickListener)
    }

    override fun setListener(listener: AdViewIntegration.Listener) {
        mListener = listener
    }

    override fun load(manager: AdViewManager): AdViewIntegration {
        mLoadFuture = CompletableFuture()
        mListener = manager.listener

        val options = HashMap<String, Any>()

        options["orientation"] = manager.orientation.toString()
        options["dimension"] = manager.size.toString()

        layoutParams = getAdLayoutParams(manager.orientation, manager.size)


        mAd = getAdService.run(options)

        if (mAd!!.type !== AdType.BANNER) {
            throw APIIOException(
                HTTPStatusCode.FORBIDDEN, AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE
            )
        }


        mCurrentRunningTime = 0


        if (mAd!!.mediaType === AdMediaType.IMAGE) {
            mImage = AdImage(context, mAd!!.mediaUrl, this)
            mImage!!.scaleType = ImageView.ScaleType.CENTER_CROP
            addView(mImage)
        } else {
            mPlayer = AdPlayer(mContext, mAd!!.mediaUrl, this)
            mPlayer!!.setScaleType(AdPlayer.ScaleType.FIT_CENTER)
            addView(mPlayer)
        }

        return mLoadFuture.get()
    }

    private fun startRunningTimer() {
        mRunningTimer?.cancel()
        mRunningTimer = Timer()

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                mCurrentRunningTime++
            }
        }
        mRunningTimer!!.scheduleAtFixedRate(task, 1000, 1000)
    }

    private fun stopRunningTimer() {
        if (mRunningTimer != null) {
            mRunningTimer!!.cancel()
            mRunningTimer = null
        }
    }

    private fun getAdDuration(): Int {
        if (mAd!!.refreshRate === Ad.AUTO_REFRESH_RATE && mAd!!.mediaType === AdMediaType.VIDEO) return mPlayer!!.adDuration

        // 0 or 30-150
        return Ad.DEFAULT_AD_DURATION
    }

    override fun onImageReady() {

        mAdDuration = getAdDuration()
        manager.refreshRate = mAdDuration
        mLoadFuture.complete(this)
        startRunningTimer()
        sendAdEventService.run(AdEventType.VIEW, mAd!!)
        mListener?.onImpression()
    }

    override fun onImageError() {
        mLoadFuture.completeExceptionally(
            APIIOException(
                HTTPStatusCode.NO_RESPONSE, AdRequestException.PLAYBACK_ERROR
            )
        )
    }

    override fun pauseAd() {
        stopRunningTimer()
        if (mAd?.mediaType === AdMediaType.VIDEO) mPlayer?.pause()
    }

    override fun resumeAd() {
        startRunningTimer()
        if (mAd?.mediaType === AdMediaType.VIDEO) mPlayer?.play()
    }

    override fun placeIn(parent: ViewGroup) {
        if (parent.indexOfChild(this) >= 0) parent.removeView(this)
        parent.addView(this)
    }

    override fun onVideoProgressChanged(position: Double, total: Double) {
        mCurrentRunningTime = position.toInt()
    }


    override fun onVideoReady(videoDuration: Int) {
        mAdDuration = getAdDuration()
        mPlayer!!.setMuted(true)
        manager.refreshRate = mAdDuration
        mLoadFuture.complete(this)

        if (showPermission) {
            mPlayer!!.play()
            startRunningTimer()
        }

        sendAdEventService.run(AdEventType.VIEW, mAd!!)
        mListener?.onImpression()
    }

    override fun onVideoFinished() {
        mListener?.onDismissed()
    }

    override fun onVideoError() {
        mLoadFuture.completeExceptionally(
            APIIOException(
                HTTPStatusCode.NO_RESPONSE, AdRequestException.PLAYBACK_ERROR
            )
        )
    }


    interface Listener : AdViewIntegration.Listener


    class Builder : AdViewManager.Builder {
        override fun build(manager: AdViewManager): AdViewIntegration {
            return AdServerAdView(
                manager, makeGetAdService(manager), makeSendAdEventService(manager)
            )
        }

        private fun makeGetAdService(manager: AdViewManager): IGetAdService {
            return GetAdService(manager)
        }

        private fun makeSendAdEventService(manager: AdViewManager): ISendAdEventService {
            return SendAdEventService(manager)
        }
    }

    companion object {
        const val TEST_UNIT_ID: String = "banner"
    }
}
