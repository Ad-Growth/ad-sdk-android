package com.adgrowth.internal.integrations.adserver.entities

import com.adgrowth.adserver.BuildConfig
import com.adgrowth.adserver.entities.RewardItem
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.internal.integrations.adserver.enums.AdMediaType
import com.adgrowth.internal.integrations.adserver.enums.AdType
import com.adgrowth.internal.integrations.adserver.helpers.JSONHelper
import org.json.JSONObject

class Ad(json: JSONObject) {
    private val mRewardValue: Int
    private val mRewardItem: String
    var impressionUrl: String
        private set
    val refreshRate: Double?
    val id: String
    val ipAddress: String
    val mediaUrl: String
    val type: AdType
    val actionUrl: String
    val postMediaUrl: String
    val mediaType: AdMediaType
    val orientation: AdOrientation
    var isConsumed = false

    init {
        val advert = JSONHelper.safeGetObject(json, "advert")
        val meta = JSONHelper.safeGetObject(json, "meta")

        // advert
        id = JSONHelper.safeGetString(advert, "id")
        mediaUrl = JSONHelper.safeGetString(advert, "media_url")
        type = AdType.valueOf(JSONHelper.safeGetString(advert, "type"))
        mediaType = AdMediaType.valueOf(JSONHelper.safeGetString(advert, "media_type"))
        actionUrl = JSONHelper.safeGetString(advert, "action_url")
        impressionUrl = JSONHelper.safeGetString(advert, "impression_url")
        postMediaUrl = JSONHelper.safeGetString(advert, "post_media_url")
        orientation = AdOrientation.valueOf(JSONHelper.safeGetString(advert, "orientation"))

        // meta
        mRewardItem = JSONHelper.safeGetString(meta, "reward_item", DEFAULT_REWARD_ITEM)!!
        mRewardValue = JSONHelper.safeGetInt(meta, "reward_value", DEFAULT_REWARD_VALUE)!!
        refreshRate = JSONHelper.safeGetInt(meta, "refresh_rate", null)?.toDouble()
        ipAddress = JSONHelper.safeGetString(meta, "ip_address")
    }

    val reward: RewardItem
        get() = RewardItem(mRewardValue, mRewardItem)

    override fun toString(): String {
        return String.format(
            """
                id: %s ;
                type: %s;
                mediaUrl: %s;
                mediaType: %s;
                reward: %s;
                actionUrl: %s;
                postAdMediaUrl: %s.""".trimIndent(),
            id,
            type,
            mediaUrl,
            mediaType,
            mRewardValue,
            actionUrl,
            postMediaUrl
        )
    }

    companion object {
        const val DEFAULT_REWARD_ITEM = "reward_item"

        @JvmField
        val AUTO_REFRESH_RATE = null
        const val DISABLED_REFRESH_RATE = 0.0
        val DEFAULT_AD_DURATION: Double = BuildConfig.DEFAULT_AD_DURATION
        const val DEFAULT_REWARD_VALUE = 1
    }
}
