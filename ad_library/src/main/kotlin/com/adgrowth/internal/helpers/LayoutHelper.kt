package com.adgrowth.internal.helpers

import android.content.res.Resources
import android.view.ViewGroup.LayoutParams
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize

class LayoutHelper {
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

            if (orientation == AdOrientation.PORTRAIT)
                return LayoutParams(dpToPx(height), dpToPx(width))

            return LayoutParams(dpToPx(width), dpToPx(height))
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

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

    }
}
