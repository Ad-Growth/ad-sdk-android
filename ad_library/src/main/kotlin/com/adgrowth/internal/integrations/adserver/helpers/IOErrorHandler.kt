package com.adgrowth.internal.integrations.adserver.helpers

import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException

object IOErrorHandler {
    fun handle(e: Exception): AdRequestException {
        if (e is APIIOException) {
            when (e.statusCode) {
                0 -> return AdRequestException(AdRequestException.NETWORK_ERROR)
                404 -> if (e.message!!.contains("No ads found")) return AdRequestException(
                    AdRequestException.NO_AD_FOUND
                )
                400 -> {
                    if (e.message!!.contains("Unit_id invalid")) return AdRequestException(
                        AdRequestException.INVALID_UNIT_ID
                    )
                    if (e.message!!.contains("Client_key invalid")) return AdRequestException(
                        AdRequestException.INVALID_CLIENT_KEY
                    )
                }
                500, 502 -> return AdRequestException(AdRequestException.INTERNAL_ERROR)
            }
        }
        return AdRequestException(AdRequestException.UNKNOWN_ERROR)
    }
}
