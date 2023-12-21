package com.adgrowth.internal.integrations.adserver.services.interfaces

import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.interfaces.managers.AdManager
import java.util.HashMap

abstract class GetAdService(protected open val manager: AdManager<*, *>) {
    @Throws(APIIOException::class)
    abstract fun run(params: HashMap<String, Any>): Ad
}
