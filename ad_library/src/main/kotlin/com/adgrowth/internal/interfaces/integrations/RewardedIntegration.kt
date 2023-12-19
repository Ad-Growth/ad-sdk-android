package com.adgrowth.internal.interfaces.integrations;

import com.adgrowth.adserver.entities.RewardItem
import com.adgrowth.internal.exceptions.APIIOException
import com.adgrowth.internal.integrations.RewardedManager
import com.adgrowth.internal.interfaces.listeners.AdListener

interface RewardedIntegration :
    AdIntegration<RewardedIntegration, RewardedIntegration.Listener> {
    @Throws(APIIOException::class)
    fun load(manager: RewardedManager): RewardedIntegration
    fun show(manager: RewardedManager)
    fun onRunningTimeChanged(elapsedTime: Int)
    interface Listener : AdListener<RewardedIntegration> {
        fun onEarnedReward(rewardItem: RewardItem)
    }
}
