package com.adgrowth.internal.interfaces.managers

import android.app.Activity
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.internal.interfaces.managers.InitializationManager as InitializationManager
import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.interfaces.integrations.InitializerIntegration

abstract class InitializationManager(
    open val context: Activity,
    open val clientKey: String,
    open val clientProfile: ClientProfile,
    open var listener: AdServer.Listener
) {
    abstract val appMetadata: AppMetaData

    interface Builder {
        fun build(manager: InitializationManager): InitializerIntegration
    }

}
