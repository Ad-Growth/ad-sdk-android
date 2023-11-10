package com.adgrowth.internal.interfaces.integration

import android.content.Context

import com.adgrowth.internal.interfaces.listeners.InitializerListener

abstract class InitializerIntegration<T, Listener>(
    protected val context: Context, protected open val clientKey: String?
) {
    protected var mListener: Listener? = null
    open fun initialize(listener: Listener) {}

    interface Listener<T, E> : InitializerListener<T, E>
}
