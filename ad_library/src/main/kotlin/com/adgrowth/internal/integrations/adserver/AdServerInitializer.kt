package com.adgrowth.internal.integrations.adserver

import android.content.Context
import com.adgrowth.adserver.AdServer
import com.adgrowth.adserver.entities.ClientProfile
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.adserver.exceptions.SDKInitException
import com.adgrowth.internal.integrations.adserver.entities.AppMetaData
import com.adgrowth.internal.integrations.adserver.services.GetAddressService
import com.adgrowth.internal.integrations.adserver.services.GetAppMetaService
import com.adgrowth.internal.interfaces.integration.InitializerIntegration
import com.adgrowth.internal.interfaces.listeners.InitializerListener
import kotlinx.coroutines.*

class AdServerInitializer(
    context: Context, override val clientKey: String
) : InitializerIntegration<AdServerInitializer, AdServerInitializer.Listener>(
    context, clientKey
) {
    private val profile: ClientProfile = AdServer.clientProfile
    private val getAppMetadataService = GetAppMetaService()
    private val getAddressService = GetAddressService()
    private var mClientKey: String? = null
    var appMetadata: AppMetaData? = null

    var isInitialized = false
        private set


    override fun initialize(listener: Listener) {
        if (this.isInitialized) {
            listener.onFailed(SDKInitException(SDKInitException.ALREADY_INITIALIZED))
            return
        }

        if (clientKey == mClientKey) {
            this.isInitialized = true
            listener.onInit(this@AdServerInitializer)
            return
        }

        val scope = CoroutineScope(Dispatchers.IO)

        val appMetadata = scope.async {
            // TODO: use service when back is available
            // appMetadata = getAppMetadataService.run()
        }

        val clientAddress = scope.async {
            try {
                val clientAddress = getAddressService.run(
                    profile.clientAddress.latitude,
                    profile.clientAddress.longitude
                )

                AdServer.clientProfile.clientAddress = clientAddress

            } catch (_: AdRequestException) {

            }
        }


        scope.launch {
            try {
                appMetadata.await()
                clientAddress.await()

                mClientKey = clientKey

                listener.onInit(this@AdServerInitializer)

            } catch (e: AdRequestException) {
                listener.onFailed(SDKInitException(e))
            }
        }


    }

    interface Listener : InitializerListener<AdServerInitializer, SDKInitException>

}
