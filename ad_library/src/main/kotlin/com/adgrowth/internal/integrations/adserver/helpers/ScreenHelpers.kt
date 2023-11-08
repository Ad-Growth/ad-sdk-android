package com.adgrowth.internal.integrations.adserver.helpers


import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import com.adgrowth.adserver.enums.AdOrientation


object ScreenHelpers {
    @JvmStatic
    fun getOrientation(context: Context): AdOrientation {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) AdOrientation.LANDSCAPE else AdOrientation.PORTRAIT
    }

    fun setOrientation(context: Activity, orientation: AdOrientation) {
        when (orientation) {
            AdOrientation.PORTRAIT -> context.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            AdOrientation.LANDSCAPE -> context.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }
}
