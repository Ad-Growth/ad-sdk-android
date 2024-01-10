package com.adgrowth.internal.interfaces.listeners

import com.adgrowth.adserver.exceptions.AdRequestException

interface AdListener<T> {
    fun onDismissed()
    fun onLoad(ad: T)
    fun onFailedToLoad(exception: AdRequestException?)

    @JvmDefault
    fun onImpression() {}

    @JvmDefault
    fun onFailedToShow(code: String?) {}

    @JvmDefault
    fun onClicked() {}
}
