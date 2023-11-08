package com.adgrowth.internal.integrations.adserver.views

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import com.adgrowth.internal.integrations.adserver.entities.Ad
import java.io.IOException
import java.util.*
import kotlin.math.ceil

@SuppressLint("ViewConstructor")
class AdPlayer(context: Activity?, url: String?, playerListener: Listener?) : TextureView(context),
    TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {


    private var scaleType = ScaleType.FIT_CENTER
    var mMediaPlayer: MediaPlayer?
    private var mCurrentPosition = 0
    private val mListener: Listener?
    private var mTimer: Timer? = null
    private var mReleased = false
    var adDuration = Ad.DEFAULT_AD_DURATION
        private set

    init {
        mMediaPlayer = MediaPlayer()
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        mListener = playerListener
        surfaceTextureListener = this

        try {
            mMediaPlayer!!.setDataSource(url)
            mMediaPlayer!!.setOnPreparedListener(this)
            mMediaPlayer!!.setOnErrorListener(this)
            mMediaPlayer!!.setOnCompletionListener(this)
            mMediaPlayer!!.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    fun setMuted(muted: Boolean) {
        val vol: Float = if (muted) 0F else 1.toFloat()
        mMediaPlayer!!.setVolume(vol, vol)
    }

    fun setScaleType(scaleType: ScaleType) {
        this.scaleType = scaleType
    }

    fun trackProgress() {
        if (mReleased || mMediaPlayer == null) return
        stopTrackingProgress()
        mTimer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                if (mListener != null) (context as Activity).runOnUiThread {
                    try {
                        mListener.onVideoProgressChanged(
                            mMediaPlayer!!.currentPosition.toDouble() / 1000.0,
                            adDuration.toDouble()
                        )
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
        mTimer!!.scheduleAtFixedRate(task, 0, 500)
    }

    private fun adjustScale(videoWidth: Int, videoHeight: Int) {
        val viewWidth = this.width
        val viewHeight = this.height

        val aspectRatio = videoWidth.toDouble() / videoHeight

        val newWidth: Int
        val newHeight: Int

        var xOffset = 0
        var yOffset = 0

        when (scaleType) {
            ScaleType.CENTER_CROP -> if (viewHeight > (viewWidth / aspectRatio).toInt()) {
                newWidth = (viewHeight * aspectRatio).toInt()
                newHeight = viewHeight
                xOffset = (viewWidth - newWidth) / 2
            } else {
                newWidth = viewWidth
                newHeight = (viewWidth / aspectRatio).toInt()
                yOffset = (viewHeight - newHeight) / 2
            }
            else -> {
                if (viewHeight > (viewWidth / aspectRatio).toInt()) {
                    newWidth = viewWidth
                    newHeight = (viewWidth / aspectRatio).toInt()
                } else {
                    newWidth = (viewHeight * aspectRatio).toInt()
                    newHeight = viewHeight
                }
                xOffset = (viewWidth - newWidth) / 2
                yOffset = (viewHeight - newHeight) / 2
            }
        }

        val matrix = Matrix()
        getTransform(matrix)

        matrix.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        matrix.postTranslate(xOffset.toFloat(), yOffset.toFloat())

        setTransform(matrix)
    }

    fun release() {
        if (mReleased || mMediaPlayer == null) return
        stopTrackingProgress()
        mMediaPlayer!!.reset()
        mMediaPlayer!!.release()
        mReleased = true
        mMediaPlayer = null
    }

    fun play() {
        if (mReleased || mMediaPlayer == null) return
        if (!mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.seekTo(mCurrentPosition)
            trackProgress()
            mMediaPlayer!!.start()
        }
        mListener?.onPlay()
    }

    fun pause() {
        if (mReleased || mMediaPlayer == null) return
        try {
            stopTrackingProgress()
            mListener?.onPause()
            mCurrentPosition = mMediaPlayer!!.currentPosition
            if (mMediaPlayer!!.isPlaying) mMediaPlayer!!.pause()
        } catch (ignored: IllegalStateException) {
            ignored.printStackTrace()
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        stopTrackingProgress()
        mListener?.onVideoFinished()
    }

    private fun stopTrackingProgress() {
        try {
            mTimer?.cancel()
            mTimer = null
        } catch (ignored: Exception) {
        }
    }

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        mListener?.onVideoError()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        adjustScale(mp.videoWidth, mp.videoHeight)
        adDuration = ceil((mp.duration / 1000).toDouble()).toInt()
        mListener?.onVideoReady(adDuration)
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        if (mReleased || mMediaPlayer == null) return
        adjustScale(mMediaPlayer!!.videoWidth, mMediaPlayer!!.videoHeight)
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        if (mReleased || mMediaPlayer == null) return
        mMediaPlayer!!.setSurface(Surface(surfaceTexture))
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
        if (mReleased || mMediaPlayer == null) return
        adjustScale(mMediaPlayer!!.videoWidth, mMediaPlayer!!.videoHeight)
    }

    interface Listener {
        fun onVideoProgressChanged(position: Double, total: Double)
        fun onVideoReady(videoDuration: Int)
        fun onPause() {}
        fun onPlay() {}
        fun onVideoFinished()
        fun onVideoError()
    }


    enum class ScaleType {
        FIT_CENTER, CENTER_CROP
    }
}