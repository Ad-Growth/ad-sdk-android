package com.adgrowth.internal.integrations.admob

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.adgrowth.internal.interfaces.integration.InitializerIntegration
import com.google.android.gms.ads.MobileAds


class AdMobInitializer(context: Context, override val clientKey: String?) :
    InitializerIntegration<AdMobInitializer, AdMobInitializer.Listener>(context, clientKey) {

    private var isInitialized = false
    var isFailed = false
        private set

    override fun initialize(listener: Listener) {
        mListener = listener

        val app: ApplicationInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )


        val adMobAppId = app.metaData.getString(ADMOB_APPLICATION_ID)

        if (adMobAppId != null) {
            // TODO: uncomment when api return the admob_app_id
            if (clientKey != adMobAppId) {
                listener.onFailed(INVALID_META_APPLICATION_ID)
                isFailed = true
                return
            }


            MobileAds.initialize(this.context) {
                isInitialized = true;
                this@AdMobInitializer.mListener?.onInit(this@AdMobInitializer)
            }
        } else {
            isFailed = true
            listener.onFailed(META_APPLICATION_ID_NOT_PROVIDED)
        }

    }


    companion object {
        const val META_APPLICATION_ID_NOT_PROVIDED: String = "meta_app_id_not_provided"

        const val INVALID_META_APPLICATION_ID: String = "google_admob_app_id_not_provided"
        private const val ADMOB_APPLICATION_ID = "com.google.android.gms.ads.APPLICATION_ID"
//        private const val ADMOB_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    }

    interface Listener : InitializerIntegration.Listener<AdMobInitializer, String>

}
