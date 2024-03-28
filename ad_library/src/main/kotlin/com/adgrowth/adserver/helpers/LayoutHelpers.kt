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
import android.view.ViewGroup.LayoutParams
import android.view.WindowInsets
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize


class LayoutHelpers(private val context: Activity) {

    private var decorView = context.window.decorView

    init {
        currentOrientation = context.resources.configuration.orientation
        currentRotation = decorView.display.rotation
        baseEdgeInsets = getBaseEdgeInsets()
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
    private fun getBaseEdgeInsets(): Rect {
        val insets = Rect(0, 0, 0, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayCutout =
                context.window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.tappableElement())

            insets.top = displayCutout.top
            insets.right = displayCutout.right
            insets.bottom = displayCutout.bottom
            insets.left = displayCutout.left
        }

        return when (decorView.display.rotation) {
            Surface.ROTATION_90 -> Rect(
                insets.top,
                insets.left,
                insets.bottom,
                insets.right
            )

            Surface.ROTATION_270 -> Rect(
                insets.top,
                insets.right,
                insets.bottom,
                insets.left
            )

            Surface.ROTATION_180 -> Rect(
                insets.right,
                insets.bottom,
                insets.left,
                insets.top
            )
            // Surface.ROTATION_0
            else -> insets
        }
    }
    // Get insets by provided rotation with base insets
    private fun getRectByRotation(rotation: Int): Rect {
        return when (rotation) {

            Surface.ROTATION_180 -> Rect(
                baseEdgeInsets.right,
                baseEdgeInsets.bottom,
                baseEdgeInsets.left,
                baseEdgeInsets.top
            )

            Surface.ROTATION_90 -> Rect(
                baseEdgeInsets.top,
                baseEdgeInsets.right,
                baseEdgeInsets.bottom,
                baseEdgeInsets.left
            )

            Surface.ROTATION_270 -> Rect(
                baseEdgeInsets.bottom,
                baseEdgeInsets.left,
                baseEdgeInsets.top,
                baseEdgeInsets.right
            )
            // Surface.ROTATION_0
            else -> baseEdgeInsets
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
        fun getAdLayoutParams(orientation: AdOrientation, size: AdSize): LayoutParams {
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

            if (orientation == AdOrientation.PORTRAIT) return LayoutParams(
                dpToPx(height), dpToPx(width)
            )

            return LayoutParams(dpToPx(width), dpToPx(height))
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        private var baseEdgeInsets: Rect = Rect()
        var currentEdgeInsets: Rect = Rect()
        var currentOrientation: Int = Surface.ROTATION_0
        var currentRotation: Int = Configuration.ORIENTATION_PORTRAIT

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
        fun addEdgeInsetsListener(listener: InsetListener) {
            edgeInsetsListeners.add(listener)
        }

        fun removeListener(listener: InsetListener) {
            edgeInsetsListeners.remove(listener)
        }

        fun getAdOrientation(): AdOrientation {
            val orientation = Resources.getSystem().configuration.orientation
            return if (orientation == Configuration.ORIENTATION_LANDSCAPE) AdOrientation.LANDSCAPE else AdOrientation.PORTRAIT
        }

        fun getScreenRotation(context: Activity): Int {
            return context.window.decorView.display.rotation
        }

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