package com.adgrowth.internal.integrations.adserver.helpers

import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode

object IOErrorHandler {
    fun handle(e: Exception): AdRequestException {
        if (e is APIIOException) {
            when (e.statusCode) {
                0 -> return AdRequestException(e.message ?: AdRequestException.NETWORK_ERROR)
                HTTPStatusCode.NOT_FOUND -> if (e.message!!.contains("No ads found")) return AdRequestException(
                    AdRequestException.NO_AD_FOUND
                )
                HTTPStatusCode.BAD_REQUEST -> {
                    if (e.message!!.contains("Unit_id invalid")) return AdRequestException(
                        AdRequestException.INVALID_UNIT_ID
                    )
                    if (e.message!!.contains("Client_key invalid")) return AdRequestException(
                        AdRequestException.INVALID_CLIENT_KEY
                    )
                }
                HTTPStatusCode.INTERNAL_ERROR, HTTPStatusCode.NOT_IMPLEMENTED, HTTPStatusCode.BAD_GATEWAY -> {
                    return AdRequestException(
                        AdRequestException.INTERNAL_ERROR
                    )
                }
            }
        }
        return AdRequestException(AdRequestException.UNKNOWN_ERROR)
    }
}
