package com.adgrowth.internal.integrations.adserver.services.interfaces

import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager

abstract class GetAppMetaService(open val manager: IInitializationManager) {
    @Throws(APIIOException::class)
    abstract fun run(): AppMetaData
}
