package com.adgrowth.internal.integrations.adserver.entities

import com.adgrowth.internal.integrations.adserver.helpers.JSONHelper
import org.json.JSONObject

data class AppMetaData(private val json: JSONObject) {
    val adMobAppId: String? = JSONHelper.safeGetString(json, "admob_app_id")
    val unityAppId: String? = JSONHelper.safeGetString(json, "unity_app_id")
    val adColonyAppId: String? = JSONHelper.safeGetString(json, "adcolony_app_id")
}
