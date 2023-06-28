package com.adgrowth.internal.entities;


import androidx.annotation.NonNull;

import com.adgrowth.adserver.entities.RewardItem;
import com.adgrowth.internal.enums.AdMediaType;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.helpers.JSONHelper;

import org.json.JSONObject;

public class Ad {
    public static final String DEFAULT_REWARD_ITEM = "reward_item";
    public final static String ALREADY_CONSUMED = "already_consumed";
    public final static String NOT_READY = "not_ready";
    private final int DEFAULT_REFRESH_RATE = 30;
    private final int DEFAULT_REWARD_VALUE = 1;
    private final int mRewardValue;
    private final int mRefreshRate;
    private final String mId;
    private final String mRewardItem;
    private final String mIpAddress;
    private final String mImpressionUrl;
    private final String mMediaUrl;
    private final AdType mType;
    private final String mActionUrl;
    private final String mPostMediaUrl;
    private final AdMediaType mMediaType;
    private boolean mConsumed = false;


    public Ad(JSONObject json) {
        JSONObject advert = JSONHelper.safeGetObject(json, "advert");
        JSONObject meta = JSONHelper.safeGetObject(json, "meta");

        // advert
        this.mId = JSONHelper.safeGetString(advert, "id");
        this.mMediaUrl = JSONHelper.safeGetString(advert, "media_url");
        this.mType = AdType.valueOf(JSONHelper.safeGetString(advert, "type"));
        this.mMediaType = AdMediaType.valueOf(JSONHelper.safeGetString(advert, "media_type"));
        this.mActionUrl = JSONHelper.safeGetString(advert, "action_url");
        this.mImpressionUrl = JSONHelper.safeGetString(advert, "impression_url");
        this.mPostMediaUrl = JSONHelper.safeGetString(advert, "post_media_url");

        // meta
        this.mRewardItem = JSONHelper.safeGetString(meta, "reward_item", DEFAULT_REWARD_ITEM);
        this.mRewardValue = JSONHelper.safeGetInt(meta, "reward_value", DEFAULT_REWARD_VALUE);
        this.mRefreshRate = JSONHelper.safeGetInt(meta, "refresh_rate", DEFAULT_REFRESH_RATE);
        this.mIpAddress = JSONHelper.safeGetString(meta, "ip_address");
    }

    public String getId() {
        return mId;
    }


    public String getMediaUrl() {
        return mMediaUrl;
    }


    public AdMediaType getMediaType() {
        return mMediaType;
    }


    public String getActionUrl() {
        return mActionUrl;
    }


    public String getPostMediaUrl() {
        return mPostMediaUrl;
    }


    public AdType getType() {
        return mType;
    }

    public RewardItem getReward() {
        return new RewardItem(this.mRewardValue, mRewardItem);
    }


    @NonNull
    @Override
    public String toString() {
        return String.format("id: %s ;\n"
                        + "type: %s;\n"
                        + "mediaUrl: %s;\n"
                        + "mediaType: %s;\n"
                        + "reward: %s;\n"
                        + "actionUrl: %s;\n"
                        + "postAdMediaUrl: %s.",
                this.mId,
                this.mType,
                this.mMediaUrl,
                this.mMediaType,
                this.mRewardValue,
                this.mActionUrl,
                this.mPostMediaUrl);
    }

    public boolean isConsumed() {
        return mConsumed;
    }

    public void setConsumed(boolean consumed) {
        this.mConsumed = consumed;
    }

    public String getIpAddress() {
        return this.mIpAddress;
    }

    public int getRefreshRate() {
        return mRefreshRate;
    }

    public String getImpressionUrl() {
        return this.mImpressionUrl;
    }
}
