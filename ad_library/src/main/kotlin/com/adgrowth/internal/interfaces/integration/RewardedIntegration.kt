package com.adgrowth.internal.interfaces.integration;

import android.app.Activity
import com.adgrowth.adserver.entities.RewardItem

interface RewardedIntegration<T : RewardedIntegration<T, Listener>, Listener : AdIntegration.Listener<T>> :
    AdIntegration<T, Listener> {
    fun load(context: Activity)

    interface Listener<T> : AdIntegration.Listener<T> {
        fun onEarnedReward(rewardItem: RewardItem?)
        fun onDismissed();
    }
}
