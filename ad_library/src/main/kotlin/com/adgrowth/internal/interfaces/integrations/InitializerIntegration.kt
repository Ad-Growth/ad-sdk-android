package com.adgrowth.internal.interfaces.integrations

import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager
import com.adgrowth.internal.interfaces.listeners.InitializerListener

abstract class InitializerIntegration(protected open val manager: IInitializationManager) {
    @kotlin.jvm.Throws(SDKInitException::class)
    abstract fun initialize(): InitializerIntegration

    interface Listener : InitializerListener<InitializerIntegration>
}
