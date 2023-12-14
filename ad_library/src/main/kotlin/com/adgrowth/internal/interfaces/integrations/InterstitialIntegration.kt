package com.adgrowth.internal.interfaces.integrations

import com.adgrowth.internal.integrations.InterstitialManager
import com.adgrowth.internal.interfaces.listeners.AdListener

interface InterstitialIntegration :
    AdIntegration<InterstitialIntegration, InterstitialIntegration.Listener> {
    fun load(manager: InterstitialManager): InterstitialIntegration
    fun show(manager: InterstitialManager)
    fun onRunningTimeChanged(elapsedTime: Int)
    interface Listener : AdListener<InterstitialIntegration>
}
