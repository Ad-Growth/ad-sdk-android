package com.adgrowth.internal.integrations.adserver.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdPlayer(context: Context, url: String, private val listener: Listener) :
    PlayerView(context), Player.Listener {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val mediaItem: MediaItem = MediaItem.fromUri(url)
    private val playerHandler = Handler(Looper.getMainLooper())

    val adDuration: Double get() = (exoPlayer.duration.toDouble() / 1000)

    private val progressUpdater = object : Runnable {
        override fun run() {
            if (exoPlayer.isPlaying) {
                listener.onVideoProgressChanged(
                    (exoPlayer.currentPosition.toDouble() / 1000),
                    (exoPlayer.duration.toDouble() / 1000)
                )
                playerHandler.postDelayed(this, 100)
            }
        }
    }

    init {
        mainScope.launch {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            useController = false
            player = this@AdPlayer.exoPlayer
            setOnClickListener { listener.onClick() }

            exoPlayer.addListener(this@AdPlayer)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    fun play() {
        mainScope.launch {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        }
    }

    fun pause() {
        mainScope.launch {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
        }
    }

    fun setMuted(mute: Boolean) {
        mainScope.launch {
            exoPlayer.volume = if (mute) 0f else 1f
        }
    }

    fun release() {
        mainScope.launch {
            playerHandler.removeCallbacks(progressUpdater)
            if (parent != null) (parent as ViewGroup).removeView(this@AdPlayer)
            exoPlayer.removeListener(this@AdPlayer)
            exoPlayer.release()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {

        when (playbackState) {
            Player.STATE_READY -> listener.onVideoReady((exoPlayer.duration.toDouble() / 1000))
            Player.STATE_ENDED -> listener.onVideoFinished()
        }

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {

        if (!isPlaying) {
            listener.onPause()
            playerHandler.removeCallbacks(progressUpdater)
        } else {
            listener.onPlay()
            playerHandler.post(progressUpdater)
        }

    }


    override fun onPlayerError(error: PlaybackException) {
        mainScope.launch {
            listener.onVideoError()
        }
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
