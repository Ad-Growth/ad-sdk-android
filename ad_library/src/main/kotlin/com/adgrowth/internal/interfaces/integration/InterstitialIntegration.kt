package com.adgrowth.internal.interfaces.integration


import android.app.Activity

import com.adgrowth.internal.interfaces.listeners.AdListener

interface InterstitialIntegration<T : InterstitialIntegration<T, Listener>, Listener : AdListener<T>> :
    AdIntegration<T, Listener> {
    fun load(context: Activity)

    interface Listener<T> : AdIntegration.Listener<T> {
        fun onDismissed()
    }
}
