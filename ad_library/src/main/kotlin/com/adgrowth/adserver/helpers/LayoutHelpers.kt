package com.adgrowth.adserver.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.Surface
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize


class LayoutHelpers(private val context: Activity) {

    private var decorView = context.window.decorView

    init {
        currentOrientation = context.resources.configuration.orientation
        currentRotation = decorView.display.rotation
        currentEdgeInsets = getRectByRotation(decorView.display.rotation)
    }

    private var mOrientationChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, myIntent: Intent) {
            if (myIntent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                val rect = getRectByRotation(decorView.display.rotation)

                currentEdgeInsets = rect
                currentOrientation = context.resources.configuration.orientation
                currentRotation = decorView.display.rotation

                notifyInsetsChanged(rect)
            }
        }
    }

    fun startEdgeInsetsObserver() {
        context.registerReceiver(
            mOrientationChangeReceiver, IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        )
    }

    // get base insets and correct it to portrait 0º rotation dimensions
    private fun setScreenInfo() {

        val windowInsetsCompat =  WindowInsetsCompat.toWindowInsetsCompat(context.window.decorView.rootWindowInsets)
        val displayCutout = windowInsetsCompat.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.displayCutout());
        val systemBars = windowInsetsCompat.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars());

        notchHeight = 0
        statusBarHeight = 0
        systemNavigationHeight = 0

        when (decorView.display.rotation) {
            Surface.ROTATION_90 -> {
                notchHeight = displayCutout.left
                statusBarHeight = systemBars.top
                systemNavigationHeight = systemBars.right
            }

            Surface.ROTATION_270 -> {
                notchHeight = displayCutout.right
                statusBarHeight = systemBars.top
                systemNavigationHeight = systemBars.left

            }

            Surface.ROTATION_180 -> {
                notchHeight = displayCutout.bottom
                statusBarHeight = systemBars.top
                systemNavigationHeight = systemBars.bottom

            }
            // Surface.ROTATION_0
            else -> {
                notchHeight = displayCutout.top
                statusBarHeight = systemBars.top
                systemNavigationHeight = systemBars.bottom
            }
        }
    }

    // Get insets by provided rotation with base insets
    private fun getRectByRotation(rotation: Int): Rect {
        setScreenInfo()

        return when (rotation) {
            // landscape
            Surface.ROTATION_90 -> Rect(
                notchHeight,
                statusBarHeight,
                systemNavigationHeight,
                0
            )
            // landscape reverse
            Surface.ROTATION_270 -> Rect(
                systemNavigationHeight,
                statusBarHeight,
                notchHeight,
                0
            )
            // portrait reverse
            Surface.ROTATION_180 -> Rect(
                0,                                                               // left
                systemNavigationHeight,                                              // top
                0,                                                              // right
                if (notchHeight > statusBarHeight) notchHeight else statusBarHeight, // bottom
            )
            // portrait
            else -> Rect(
                0,
                if (notchHeight > statusBarHeight) notchHeight else statusBarHeight,
                0,
                systemNavigationHeight
            )
        }
    }


    fun stopEdgeInsetsObserver() {
        context.unregisterReceiver(mOrientationChangeReceiver)
        edgeInsetsListeners.clear()
    }

    private fun notifyInsetsChanged(rect: Rect) {
        for (listener in edgeInsetsListeners) {
            listener.onInsetsChanged(rect)
        }
    }


    interface InsetListener {
        fun onInsetsChanged(insets: Rect)
    }

    companion object {
        @JvmStatic
        fun getAdViewLayoutParams(
            orientation: AdOrientation,
            size: AdSize
        ): FrameLayout.LayoutParams {
            val width: Int
            val height: Int

            when (size) {
                AdSize.FULL_BANNER -> {
                    width = FULL_BANNER_WIDTH
                    height = FULL_BANNER_HEIGHT
                }

                AdSize.LARGE_BANNER -> {
                    width = LARGE_BANNER_WIDTH
                    height = LARGE_BANNER_HEIGHT
                }

                AdSize.LEADERBOARD -> {
                    width = LEADERBOARD_WIDTH
                    height = LEADERBOARD_HEIGHT
                }

                AdSize.MEDIUM_RECTANGLE -> {
                    width = MEDIUM_RECTANGLE_WIDTH
                    height = MEDIUM_RECTANGLE_HEIGHT
                }

                else -> {
                    width = BANNER_WIDTH
                    height = BANNER_HEIGHT
                }
            }

            if (orientation == AdOrientation.PORTRAIT) return FrameLayout.LayoutParams(
                dpToPx(height), dpToPx(width)
            )

            return FrameLayout.LayoutParams(dpToPx(width), dpToPx(height))
        }

        @JvmStatic
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        @JvmStatic
        var currentEdgeInsets: Rect = Rect()
            private set

        @JvmStatic
        var currentOrientation: Int = Surface.ROTATION_0
            private set

        @JvmStatic
        var currentRotation: Int = Configuration.ORIENTATION_PORTRAIT
            private set

        @JvmStatic
        var notchHeight = 0
            private set

        @JvmStatic
        var statusBarHeight = 0
            private set

        @JvmStatic
        var systemNavigationHeight = 0
            private set

        @JvmStatic
        var displayWidth = Resources.getSystem().displayMetrics.widthPixels
            private set

        @JvmStatic
        var displayHeight = Resources.getSystem().displayMetrics.heightPixels
            private set

        private const val BANNER_WIDTH = 320
        private const val BANNER_HEIGHT = 50

        private const val FULL_BANNER_WIDTH = 468
        private const val FULL_BANNER_HEIGHT = 60

        private const val LARGE_BANNER_WIDTH = 320
        private const val LARGE_BANNER_HEIGHT = 100

        private const val LEADERBOARD_WIDTH = 728
        private const val LEADERBOARD_HEIGHT = 90

        private const val MEDIUM_RECTANGLE_WIDTH = 300
        private const val MEDIUM_RECTANGLE_HEIGHT = 250

        private val edgeInsetsListeners = mutableListOf<InsetListener>()


        @JvmStatic
        fun addEdgeInsetsListener(listener: InsetListener) {
            edgeInsetsListeners.add(listener)
        }

        @JvmStatic
        fun removeListener(listener: InsetListener) {
            edgeInsetsListeners.remove(listener)
        }

        @JvmStatic
        fun getAdOrientation(): AdOrientation {
            val orientation = Resources.getSystem().configuration.orientation
            return if (orientation == Configuration.ORIENTATION_LANDSCAPE) AdOrientation.LANDSCAPE else AdOrientation.PORTRAIT
        }

        @JvmStatic
        fun getScreenRotation(context: Activity): Int {
            return context.window.decorView.display.rotation
        }

        @JvmStatic
        @SuppressLint("SourceLockedOrientationActivity")
        fun setScreenRotation(context: Activity, orientation: AdOrientation?) {
            val currentRotation = getScreenRotation(context)
            val isCompatibleWithCurrentOrientation =
                (orientation == AdOrientation.PORTRAIT && (currentRotation == Surface.ROTATION_0 || currentRotation == Surface.ROTATION_180)) || (orientation == AdOrientation.LANDSCAPE && (currentRotation == Surface.ROTATION_90 || currentRotation == Surface.ROTATION_270))

            if (isCompatibleWithCurrentOrientation) return

            when (orientation) {
                AdOrientation.PORTRAIT -> context.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                AdOrientation.LANDSCAPE -> context.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                else -> context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}