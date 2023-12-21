package com.adgrowth.internal.integrations.admob.services.interfaces

import com.adgrowth.internal.enums.AdEventType
import com.adgrowth.internal.interfaces.managers.AdManager

abstract class SendAdEventService(protected open val manager: AdManager<*, *>) {
    abstract fun run(type: AdEventType)
}
