package com.adgrowth.internal.interfaces.integrations

import com.adgrowth.internal.interfaces.listeners.AdListener


interface AdIntegration<T, Listener : AdListener<T>> {
    fun setListener(listener: Listener)
}
