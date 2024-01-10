package com.adgrowth.internal.integrations.admob.services.interfaces

import com.adgrowth.internal.interfaces.managers.AdManager
import com.google.android.gms.ads.AdRequest

abstract class GetAdService<T>(protected open val manager: AdManager<*, *>) {
    abstract fun run(adRequest: AdRequest): T
}
