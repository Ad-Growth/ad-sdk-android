package com.adgrowth.internal.integrations.adserver.services

import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.adserver.http.HttpClient
import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.integrations.adserver.exceptions.APIIOException
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler

class GetAppMetaService(private val clientKey: String) {

    private val mHttpClient: HttpClient = HttpClient()

    @Throws(AdRequestException::class)
    fun run(): AppMetaData {

        val params = HashMap<String, Any>()

        params["client_key"] = clientKey

        return try {
            val response = mHttpClient["/adserver/api/app_meta", params]
            AppMetaData(response)
        } catch (e: APIIOException) {
            throw IOErrorHandler.handle(e)
        }
    }
}
