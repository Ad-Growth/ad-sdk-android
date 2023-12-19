package com.adgrowth.internal.integrations.adserver.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.adgrowth.internal.integrations.InitializationManager
import java.util.*

object AdUriHelpers {
    @JvmStatic
    fun openUrl(context: Activity, url: String, ipAddress: String?) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(replaceAdCallbackParams(context, url, ipAddress)))
        context.startActivity(intent)
    }

    @JvmStatic
    fun replaceAdCallbackParams(context: Activity, uri: String, ipAddress: String?): String {
        val clickId = UUID.randomUUID().toString()
        val adId = InitializationManager.ADVERTISING_ID
        val siteId = context.packageName

        return uri
            .replace("\\{advertising_id\\}".toRegex(), adId)
            .replace("\\{click_id\\}".toRegex(), clickId)
            .replace("\\{site_id\\}".toRegex(), siteId)
            .replace("\\{ip\\}".toRegex(), ipAddress!!)
    }
}
