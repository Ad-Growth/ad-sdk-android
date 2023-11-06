package com.adgrowth.internal.interfaces.listeners

import com.adgrowth.adserver.exceptions.AdRequestException

interface AdListener<T> {
    fun onLoad(ad: T)
    fun onFailedToLoad(exception: AdRequestException?)
    fun onImpression() {}
    fun onFailedToShow(code: String?) {}
    fun onClicked() {}
}
