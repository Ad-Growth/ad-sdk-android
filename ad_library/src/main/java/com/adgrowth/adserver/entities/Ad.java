package com.adgrowth.adserver.entities;


import androidx.annotation.NonNull;

import com.adgrowth.adserver.constants.AdMediaType;
import com.adgrowth.adserver.constants.AdType;
import com.adgrowth.adserver.helpers.JSONHelper;

import org.json.JSONObject;

public class Ad {
    public final static String ALREADY_LOADED = "already_loaded";
    public final static String ALREADY_CONSUMED = "already_consumed";
    public final static String NOT_READY = "not_ready";
    public final static String MEDIA_ERROR = "media_error";
    private final String id;
    private final AdType type;
    private final String mediaUrl;
    private final AdMediaType mediaType;
    private final int reward;
    private String actionUrl;
    private String postMediaUrl;
    private boolean consumed = false;



    public Ad(JSONObject json) {
        this.id = JSONHelper.safeGetString(json, "id");
        this.mediaUrl = JSONHelper.safeGetString(json, "media_url");
        this.type = AdType.valueOf(JSONHelper.safeGetString(json, "type"));
        this.mediaType = AdMediaType.valueOf(JSONHelper.safeGetString(json, "media_type"));
        this.reward = JSONHelper.safeGetInt(json, "reward");
        this.actionUrl = JSONHelper.safeGetString(json, "action_url");
        this.postMediaUrl = JSONHelper.safeGetString(json, "post_media_url");
    }

    public String getId() {
        return id;
    }


    public String getMediaUrl() {
        return mediaUrl;
    }


    public AdMediaType getMediaType() {
        return mediaType;
    }


    public String getActionUrl() {
        return actionUrl;
    }


    public String getPostMediaUrl() {
        return postMediaUrl;
    }


    public AdType getType() {
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
                        + "postAdMediaUrl: %s.",
                this.id,
                this.type,
                this.mediaUrl,
                this.mediaType,
                this.reward,
                this.actionUrl,
                this.postMediaUrl);
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
