package com.adgrowth.internal.integrations.adserver.services.interfaces

import com.adgrowth.adserver.entities.ClientAddress
import com.adgrowth.internal.exceptions.APIIOException

abstract class GetAddressService {
    @Throws(APIIOException::class)
    abstract fun run(latitude: Double?, longitude: Double?): ClientAddress
}
