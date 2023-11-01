package com.adgrowth.internal.interfaces.integration

import com.adgrowth.internal.interfaces.listeners.AdListener


interface AdIntegration<T, Listener : AdListener<T>> {
    fun setListener(listener: Listener)
    fun isLoaded(): Boolean
    fun isFailed(): Boolean

    interface Listener<T> : AdListener<T>
}
