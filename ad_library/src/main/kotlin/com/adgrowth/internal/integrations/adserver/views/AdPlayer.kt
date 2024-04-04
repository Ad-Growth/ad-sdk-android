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

class AdPlayer(private val context: Context, url: String, private val listener: Listener) :
    Player.Listener {
    private var playerView: PlayerView? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val mediaItem: MediaItem = MediaItem.fromUri(url)
    private val playerHandler = Handler(Looper.getMainLooper())

    val adDuration: Double get() = player.duration.toDouble()

    private val progressUpdater = object : Runnable {
        override fun run() {
            if (player.isPlaying) {
                listener.onVideoProgressChanged(
                    player.currentPosition.toDouble(),
                    player.duration.toDouble()
                )
                playerHandler.postDelayed(this, 100)
            }
        }
    }

    init {
        mainScope.launch {
            player.addListener(this@AdPlayer)
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    fun play() {
        mainScope.launch {
            player.playWhenReady = true
            playerHandler.post(progressUpdater)
            listener.onPlay()
        }
    }

    fun pause() {
        mainScope.launch {
            player.playWhenReady = false
            playerHandler.removeCallbacks(progressUpdater)
            listener.onPause()
        }
    }

    fun setMuted(mute: Boolean) {
        mainScope.launch {
            player.volume = if (mute) 0f else 1f
        }
    }

    fun release() {
        mainScope.launch {
            playerHandler.removeCallbacks(progressUpdater)
            if (playerView?.parent != null) (playerView?.parent as ViewGroup).removeView(playerView)
            player.removeListener(this@AdPlayer)
            player.release()
        }
    }

    fun addInto(parent: ViewGroup) {
        mainScope.launch {
            playerView = PlayerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = false
                player = this@AdPlayer.player
                setOnClickListener { listener.onClick() }
            }

            if (playerView?.parent != null) (playerView?.parent as ViewGroup).removeView(playerView)

            parent.addView(playerView)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {

        when (playbackState) {
            Player.STATE_READY -> listener.onVideoReady(player.duration.toDouble())
            Player.STATE_ENDED -> listener.onVideoFinished()
        }

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {

        if (!isPlaying) {
            listener.onPause()
        } else {
            listener.onPlay()
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
