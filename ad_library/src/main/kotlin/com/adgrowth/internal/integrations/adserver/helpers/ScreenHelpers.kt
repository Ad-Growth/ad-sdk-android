package com.adgrowth.internal.integrations.adserver.helpers


import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import com.adgrowth.adserver.enums.AdOrientation


object ScreenHelpers {
    @JvmStatic
    fun getOrientation(): AdOrientation {
        val orientation = Resources.getSystem().configuration.orientation
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
