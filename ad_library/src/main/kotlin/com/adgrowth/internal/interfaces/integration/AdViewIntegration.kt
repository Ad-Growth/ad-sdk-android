package com.adgrowth.internal.interfaces.integration

import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSizeType
import com.adgrowth.internal.interfaces.listeners.AdListener

interface AdViewIntegration<T : AdIntegration<T, Listener>, Listener : AdListener<T>> :
    AdIntegration<T, Listener> {
    fun getSize(): AdSizeType
    fun getOrientation(): AdOrientation
    fun reload()

    interface Listener<T> : AdListener<T>
}
