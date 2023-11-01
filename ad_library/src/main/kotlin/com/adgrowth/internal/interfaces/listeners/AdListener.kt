package com.adgrowth.internal.interfaces.listeners

import com.adgrowth.adserver.exceptions.APIRequestException

interface AdListener<T> {
    fun onLoad(ad: T)
    fun onFailedToLoad(exception: APIRequestException?)
    fun onImpression() {}
    fun onFailedToShow(code: String?) {}
    fun onClicked() {}
}
