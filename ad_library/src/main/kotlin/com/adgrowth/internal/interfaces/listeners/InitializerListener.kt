package com.adgrowth.internal.interfaces.listeners

interface InitializerListener<T, E> {
    fun onInit(initializer: T)
    fun onFailed(e: E)
}
