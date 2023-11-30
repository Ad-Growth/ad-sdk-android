package com.adgrowth.internal.integrations.admob.services.interfaces

import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager


abstract class AdMobInitializeService(open val manager: IInitializationManager) {
    abstract fun run()
}
