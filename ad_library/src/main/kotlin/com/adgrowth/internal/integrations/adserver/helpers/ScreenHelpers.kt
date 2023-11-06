package com.adgrowth.internal.integrations.adserver.helpers

import android.content.Context
import android.content.res.Configuration

object ScreenHelpers {
    @JvmStatic
    fun getOrientation(context: Context): String {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) "LANDSCAPE" else "PORTRAIT"
    }
}
