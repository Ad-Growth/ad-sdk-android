package com.adgrowth.adserver;

import android.app.Activity;

import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.internal.enums.AdEventType;

import com.adgrowth.internal.entities.Ad;
import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.exceptions.APIIOException;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.helpers.IOErrorHandler;
import com.adgrowth.internal.helpers.ScreenHelpers;
import com.adgrowth.internal.helpers.SystemInfoHelpers;
import com.adgrowth.internal.http.APIClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class AdRequest {
    private final Activity context;
    private final APIClient apiClient;

    public AdRequest(Activity context) {
        this.context = context;
        this.apiClient = new APIClient();
    }

    public Ad getAd(String unitId, HashMap<String, Object> options) throws AdRequestException {

        ClientProfile profile = AdServer.getUserProfile();
        HashMap<String, Object> params = options;
        if (options == null) params = new HashMap<>();

        params.put("unit_id", unitId);

        if (profile.getMinAge() != 0 || profile.getMaxAge() != 0) {
            params.put("min_age", profile.getMinAge());
            params.put("max_age", profile.getMaxAge());
        } else if (profile.getAge() != 0)
            params.put("age", profile.getAge());


        params.put("gender", profile.getGender().toString());
        params.put("interests", profile.getInterests());

        if (!params.containsKey("orientation"))
            params.put("orientation", ScreenHelpers.getOrientation(context));

        ClientAddress clientAddress = profile.getClientAddress();

        params.put("city", clientAddress.getCity());
        params.put("state", clientAddress.getState());
        params.put("country", clientAddress.getCountry());

//        TODO: send later
//        HashMap systemInfo = SystemInfoHelpers.getSystemInfo(context);
//        params.put("system_info", systemInfo);


        try {
            JSONObject response = apiClient.get("/adserver/api/adverts/search", params);

            return new Ad(response.getJSONObject("advert"));

        } catch (APIIOException | JSONException e) {
            throw IOErrorHandler.handle(e);
        }

    }

    public void sendEvent(Ad ad, AdEventType eventType) {
        new Thread(() -> {

            try {

                HashMap params = new HashMap<String, String>();
                params.put("id", ad.getId());
                params.put("event_type", eventType);

                apiClient.post("/event", params);
            } catch (APIIOException e) {
                // TODO: handle it and delivery a code?
                // e.printStackTrace();
            }

        }).start();


    }

}


