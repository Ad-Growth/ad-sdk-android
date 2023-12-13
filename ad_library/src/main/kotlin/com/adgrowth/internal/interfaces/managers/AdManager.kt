package com.adgrowth.internal.interfaces.managers

import android.app.Activity
import com.adgrowth.adserver.entities.RewardItem

abstract class AdManager<Listener, Builder> {
    var refreshRate: Int? = null
    lateinit var context: Activity
    lateinit var adId: String
    abstract val unitId: String
    lateinit var ipAddress: String
    lateinit var reward: RewardItem
    abstract var listener: Listener
    protected lateinit var integrations: List<Builder>
    protected var builder: Builder? = null

    protected fun getNextIntegration(): Builder? {
        if (builder == null) {
            if (integrations.isNotEmpty()) return integrations[0]
            return null
        }

        val index = integrations.indexOf(builder!!)
        if (index >= 0 && (index + 1) < integrations.size) return integrations[index + 1]

        return null
    }
}
