package com.adgrowth.internal.integrations.adserver.entities

import com.adgrowth.internal.integrations.InitializationManager.Companion.APP_PACKAGE_NAME
import com.adgrowth.internal.integrations.admob.AdMobInitializer
import com.adgrowth.internal.integrations.adserver.helpers.JSONHelper
import org.json.JSONObject

data class AppMetaData(val json: JSONObject) {
    val appId: String = JSONHelper.safeGetString(json, "app_id", APP_PACKAGE_NAME)!!
    val ipAddress: String = JSONHelper.safeGetString(json, "ip_address")
    val isDevKey: Boolean = JSONHelper.safeGetBoolean(json, "is_dev_key", false)
    var adMob: Integration? = null
    var unity: Integration? = null
    var adColony: Integration? = null

    init {
        val integrations = JSONHelper.safeGetArray(json, "integrations")
        (0 until integrations.length()).forEach {
            val integration = integrations.getJSONObject(it)

            when (integration.getString("type")) {
                AdMobInitializer.INTEGRATION_TYPE -> adMob = Integration(integration)
                // UnityInitializer.INTEGRATION_TYPE -> unity = Integration(integration)
                // AdColonyInitializer.INTEGRATION_TYPE -> adColony = Integration(integration)
            }
        }
    }

    data class Integration(
        val json: JSONObject
    ) {
        val appId: String? = JSONHelper.safeGetString(json, "app_id", null)
        val interstitialUnitId: String? =
            JSONHelper.safeGetString(json, "interstitial_unit_id", null)
        val rewardedUnitId: String? = JSONHelper.safeGetString(json, "rewarded_unit_id", null)
        val bannerUnitId: String? = JSONHelper.safeGetString(json, "banner_unit_id", null)
    }
}
