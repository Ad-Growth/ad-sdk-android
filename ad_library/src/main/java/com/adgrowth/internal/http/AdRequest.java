package com.adgrowth.internal.http;

import android.app.Activity;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.BuildConfig;
import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.internal.enums.AdEventType;
import com.adgrowth.internal.exceptions.APIIOException;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.entities.Ad;

import com.adgrowth.internal.helpers.IOErrorHandler;
import com.adgrowth.internal.helpers.AdUriHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;


public class AdRequest {

    private final APIClient mApiClient;


    private final String mUnitId;

    public AdRequest(String unit_id) {
        this.mUnitId = unit_id;
        this.mApiClient = new APIClient();
    }

    public String getUnitId() {
        return mUnitId;
    }

    public Ad getAd(HashMap<String, Object> options) throws AdRequestException {
        if (!AdServer.isInitialized()) {
            throw new AdRequestException(AdRequestException.SDK_NOT_INITIALIZED);
        }
        ClientProfile profile = AdServer.getClientProfile();
        HashMap<String, Object> params = options;
        if (options == null) params = new HashMap<>();

        params.put("unit_id", mUnitId);

        if (profile.getMinAge() != 0 || profile.getMaxAge() != 0) {
            params.put("min_age", profile.getMinAge());
            params.put("max_age", profile.getMaxAge());
        } else if (profile.getAge() != 0) params.put("age", profile.getAge());


        params.put("gender", profile.getGender().toString());
        params.put("interests", profile.getInterests());

        ClientAddress clientAddress = profile.getClientAddress();

        params.put("city", clientAddress.getCity());
        params.put("state", clientAddress.getState());
        params.put("country", clientAddress.getCountry());

//        TODO: send later
//        HashMap systemInfo = SystemInfoHelpers.getSystemInfo(context);
//        params.put("system_info", systemInfo);

        try {
            JSONObject response = mApiClient.get("/adserver/api/adverts/search", params);
            if (params.containsKey("orientation")) {
                try {
                    response.getJSONObject("advert").put("orientation", params.get("orientation"));
                } catch (JSONException ignore) {

                }
            }
            return new Ad(response);
        } catch (APIIOException e) {
            throw IOErrorHandler.handle(e);
        }
    }

    public void sendImpression(Activity context, Ad ad) {

        sendAdEvent(context, AdEventType.VIEW, ad);

        try {
            String impression_url = AdUriHelpers.replaceAdCallbackParams(context, ad.getImpressionUrl(), ad.getIpAddress());

            APIClient impressionClient = new APIClient(impression_url);

            impressionClient.get("", new HashMap<>());

        } catch (APIIOException ignored) {
            // TODO: ignore?
            if (BuildConfig.DEBUG) {
                ignored.printStackTrace();
            }
        }

    }

    public void sendClick(Activity context, Ad ad) {
        sendAdEvent(context, AdEventType.CLICK, ad);
        AdUriHelpers.openUrl(context, ad.getActionUrl(), ad.getIpAddress());
    }

    private void sendAdEvent(Activity context, AdEventType type, Ad ad) {

        (new Thread(() -> {
            try {
                HashMap<String, Object> params = new HashMap<>();

                params.put("unit_id", mUnitId);
                params.put("type", type.toString());
                params.put("ad_id", ad.getId());
                params.put("ip", ad.getIpAddress());
                params.put("click_id", UUID.randomUUID().toString());
                params.put("site_id", context.getPackageName());
                params.put("advertising_id", AdServer.getAdId());

                mApiClient.get("/adserver/api/adverts/events", params);
            } catch (APIIOException ignored) {
                // TODO: ignore?
                if (BuildConfig.DEBUG) {
                    ignored.printStackTrace();
                }
            }
        })).start();


    }


}


