package com.adgrowth.internal.integrations.adserver.services

import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.http.HttpClient
import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager
import com.adgrowth.internal.integrations.adserver.helpers.IOErrorHandler
import org.json.JSONArray
import org.json.JSONObject
import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAppMetaService as IGetAppMetaService

class GetAppMetaService(override val manager: IInitializationManager) :
    IGetAppMetaService(manager) {

    private val mHttpClient: HttpClient = HttpClient()

    @Throws(AdRequestException::class)
    override fun run(): AppMetaData {
        try {

            val response = mHttpClient["/adserver/api/apps/app_meta", HashMap()]
            return AppMetaData(response)
        } catch (e: APIIOException) {
            throw IOErrorHandler.handle(e)
        }
    }
}
