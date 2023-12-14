package com.adgrowth.internal.integrations.admob

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode

import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager
import com.adgrowth.internal.interfaces.integrations.InitializerIntegration as IInitializerIntegration
import com.adgrowth.internal.integrations.admob.services.AdMobInitializeService
import com.adgrowth.internal.integrations.admob.services.interfaces.AdMobInitializeService as IAdMobInitializeService


class AdMobInitializer(
    override val manager: IInitializationManager,
    private val appId: String?,
    private val initializeService: IAdMobInitializeService
) : IInitializerIntegration(manager) {

    private var isInitialized = false

    override fun initialize(): IInitializerIntegration {


        val app: ApplicationInfo = manager.context.packageManager.getApplicationInfo(
            manager.context.packageName, PackageManager.GET_META_DATA
        )

        val adMobAppId = app.metaData.getString(ADMOB_APPLICATION_ID)

        if (adMobAppId != null) {
            if (appId != adMobAppId) {
                throw APIIOException(HTTPStatusCode.NO_CONTENT, INVALID_META_APPLICATION_ID)
            }

            initializeService.run()
            isInitialized = true
            return this
        } else {
            throw APIIOException(HTTPStatusCode.NO_CONTENT, META_APPLICATION_ID_NOT_PROVIDED)
        }
    }

    class Builder : IInitializationManager.Builder {
        override fun build(manager: IInitializationManager): IInitializerIntegration {
            return AdMobInitializer(
                manager, manager.appMetadata.adMob!!.appId, makeAdMobInitializeService(manager)
            )
        }

        private fun makeAdMobInitializeService(manager: IInitializationManager): IAdMobInitializeService {
            return AdMobInitializeService(manager)
        }
    }

    companion object {
        const val META_APPLICATION_ID_NOT_PROVIDED: String = "meta_app_id_not_provided"
        const val INVALID_META_APPLICATION_ID: String = "google_admob_app_id_not_provided"
        private const val ADMOB_APPLICATION_ID = "com.google.android.gms.ads.APPLICATION_ID"
        const val INTEGRATION_TYPE = "admob"
    }


}
