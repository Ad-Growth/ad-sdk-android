package com.adgrowth.internal.integrations.adserver.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.adgrowth.internal.integrations.InitializationManager
import java.util.*

object AdUriHelpers {
    @JvmStatic
    fun openUrl(context: Activity, url: String, ipAddress: String, uniqueId: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(replaceURISnippets(url, ipAddress, uniqueId)))
        context.startActivity(intent)
    }

    @JvmStatic
    fun replaceURISnippets(uri: String, ipAddress: String, uniqueId: String): String {
        val adId = InitializationManager.ADVERTISING_ID
        val appId = InitializationManager.APP_META_DATA.appId

        return uri
            .replace("\\{advertising_id\\}".toRegex(), adId)
            .replace("\\{click_id\\}".toRegex(), uniqueId)
            .replace("\\{site_id\\}".toRegex(), appId)
            .replace("\\{ip\\}".toRegex(), ipAddress)
    }

    fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}
