package com.adgrowth.adserver.entities

import com.adgrowth.internal.integrations.adserver.helpers.JSONHelper
import org.json.JSONObject

class ClientAddress(json: JSONObject) {
    var city: String?
    var state: String?
    var country: String?
    var latitude: Double?
    var longitude: Double?

    init {
        city = JSONHelper.safeGetString(json, "city")
        state = JSONHelper.safeGetString(json, "state")
        country = JSONHelper.safeGetString(json, "country")
        latitude = JSONHelper.safeGetDouble(json, "latitude")
        longitude = JSONHelper.safeGetDouble(json, "longitude")
    }
}
