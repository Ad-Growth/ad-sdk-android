package com.adgrowth.internal.integrations.adserver

import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.adserver.exceptions.SDKInitException.Companion.ALREADY_INITIALIZED
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.http.HTTPStatusCode
import com.adgrowth.internal.interfaces.managers.InitializationManager as IInitializationManager
import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.integrations.adserver.services.GetAddressService
import com.adgrowth.internal.integrations.adserver.services.GetAppMetaService

import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAddressService as IGetAddressService
import com.adgrowth.internal.integrations.adserver.services.interfaces.GetAppMetaService as IGetAppMetaService
import com.adgrowth.internal.interfaces.integrations.InitializerIntegration
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture


class AdServerInitializer(
    override val manager: IInitializationManager,
    private val getAppMetaService: IGetAppMetaService,
    private val getAddressService: IGetAddressService,
) : InitializerIntegration(
    manager
) {
    private val profile: ClientProfile = manager.clientProfile
    lateinit var appMetadata: AppMetaData

    var isInitialized = false
        private set


    override fun initialize(): InitializerIntegration {
        if (this.isInitialized) throw APIIOException(HTTPStatusCode.NO_CONTENT, ALREADY_INITIALIZED)

        val future = CompletableFuture<InitializerIntegration>()

        val scope = CoroutineScope(Dispatchers.IO)

        val appMetadata = scope.async {
            appMetadata = getAppMetaService.run()
        }

        val clientAddress = scope.async {
            try {
                val clientAddress = getAddressService.run(
                    profile.clientAddress.latitude, profile.clientAddress.longitude
                )

                profile.clientAddress = clientAddress

            } catch (_: AdRequestException) {

            }
        }


        scope.launch {
            try {
                appMetadata.await()
                clientAddress.await()

                future.complete(this@AdServerInitializer)

            } catch (e: APIIOException) {
                future.completeExceptionally(e)
            } catch (e: AdRequestException) {
                future.completeExceptionally(e)
            }
        }

        return future.get()
    }

    class Builder : IInitializationManager.Builder {
        override fun build(manager: IInitializationManager): InitializerIntegration {
            return AdServerInitializer(
                manager, makeGetAppMetaService(manager), makeGetAddressService()
            )
        }

        private fun makeGetAppMetaService(manager: IInitializationManager): GetAppMetaService {
            return GetAppMetaService(manager)
        }

        private fun makeGetAddressService(): GetAddressService {
            return GetAddressService()
        }

    }
}
