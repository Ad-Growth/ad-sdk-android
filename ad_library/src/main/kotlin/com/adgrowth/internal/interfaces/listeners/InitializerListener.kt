package com.adgrowth.internal.interfaces.listeners

import com.adgrowth.adserver.exceptions.SDKInitException

interface InitializerListener<T> {
    fun onInit(initializer: T)
    fun onFailed(e: SDKInitException, initializer: T)
}
