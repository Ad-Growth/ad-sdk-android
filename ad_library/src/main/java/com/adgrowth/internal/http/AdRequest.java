package com.adgrowth.internal.http;

import android.app.Activity;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.internal.exceptions.APIIOException;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.entities.Ad;

import com.adgrowth.internal.helpers.IOErrorHandler;
import com.adgrowth.internal.helpers.AdUriHelpers;
import com.adgrowth.internal.helpers.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class AdRequest {

    private final APIClient mApiClient;

    public AdRequest() {
        this.mApiClient = new APIClient();
    }

    public Ad getAd(String unitId, HashMap<String, Object> options) throws AdRequestException {
        if (!AdServer.isInitialized()) {
            throw new AdRequestException(AdRequestException.SDK_NOT_INITIALIZED);
        }
        ClientProfile profile = AdServer.getUserProfile();
        HashMap<String, Object> params = options;
        if (options == null) params = new HashMap<>();

        params.put("unit_id", unitId);

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

            return new Ad(response);
        } catch (APIIOException e) {
            throw IOErrorHandler.handle(e);
        }
    }

    public void sendImpression(Activity context, Ad ad) {

        try {
            String impression_url = AdUriHelpers.replaceAdCallbackParams(context, ad.getImpressionUrl(), ad.getIpAddress());

            APIClient mApiClient = new APIClient(impression_url);

            mApiClient.get("", new HashMap<>());

        } catch (APIIOException e) {
            // TODO: handle it?
            e.printStackTrace();
        }

    }

}


