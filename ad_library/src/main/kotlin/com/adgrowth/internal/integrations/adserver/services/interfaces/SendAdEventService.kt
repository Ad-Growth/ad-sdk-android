package com.adgrowth.internal.integrations.adserver.services.interfaces

import com.adgrowth.internal.integrations.adserver.entities.Ad
import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.interfaces.managers.AdManager

abstract class SendAdEventService(protected open val manager: AdManager<*, *>) {
    abstract fun run(type: AdEventType, ad: Ad)
}
