package com.adgrowth.internal.interfaces.integrations

import android.content.Context
import android.view.ViewGroup
import com.adgrowth.internal.integrations.AdViewManager
import com.adgrowth.internal.views.CenteredChildrenView
import com.adgrowth.internal.interfaces.listeners.AdListener

abstract class AdViewIntegration(context: Context) : CenteredChildrenView(context),
    AdIntegration<AdViewIntegration, AdViewIntegration.Listener> {
    abstract fun load(manager: AdViewManager): AdViewIntegration

    abstract fun hide()
    abstract fun unhide()
    abstract fun resumeAd()
    abstract fun pauseAd()
    abstract fun placeIn(parent: ViewGroup)

    interface Listener : AdListener<AdViewIntegration> {
        fun onFinished()
    }
}
