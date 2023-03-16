package com.adgrowth.adserver.entities;


import androidx.annotation.NonNull;

import com.adgrowth.adserver.helpers.JSONHelper;

import org.json.JSONObject;

public class Ad {


    private final String id;
    private final int type;
    private final String mediaUrl;
    private final int mediaType;
    private final int reward;
    private String actionUrl;
    private String postAdMediaUrl;


    public Ad(JSONObject json) {
        this.id = JSONHelper.safeGetString(json, "id");
        this.mediaUrl = JSONHelper.safeGetString(json, "media_url");
        this.type = JSONHelper.safeGetInt(json, "type");
        this.mediaType = JSONHelper.safeGetInt(json, "media_type");
        this.reward = JSONHelper.safeGetInt(json, "reward");
        this.actionUrl = JSONHelper.safeGetString(json, "action_url");
        this.postAdMediaUrl = JSONHelper.safeGetString(json, "post_ad_media_url");
    }

    public String getId() {
        return id;
    }


    public String getMediaUrl() {
        return mediaUrl;
    }


    public int getMediaType() {
        return mediaType;
    }


    public String getActionUrl() {
        return actionUrl;
    }


    public String getPostAdMediaUrl() {
        return postAdMediaUrl;
    }


    public int getType() {
        return type;
    }

    public int getReward() {
        return reward;
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
                        + "postAdMediaUrl: %s;",
                this.id,
                this.type,
                this.mediaUrl,
                this.mediaType,
                this.reward,
                this.actionUrl,
                this.postAdMediaUrl);
    }
}
