package com.adgrowth.adserver.http;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.entities.Ad;
import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.helpers.SystemInfoHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;


public class AdRequest {
    private final Context context;

    public AdRequest(Context context) {
        this.context = context;
    }

    public Ad getAd(int adType, String unitId) throws AdRequestException {


        HashMap<String, String> headers = new HashMap();
        headers.put("client_key", AdServer.getClientKey());
        HashMap systemInfo = SystemInfoHelpers.getSystemInfo(context);
        ClientProfile profile = AdServer.getUserProfile();
        ClientAddress clientAddress = AdServer.getLocation();

        HashMap params = new HashMap<String, String>();

        params.put("ad_type", adType);
        params.put("unit_id", unitId);

        params.put("system_info", systemInfo);

        // user profile data
        params.put("age", profile.getAge());
        params.put("gender", profile.getGender());
        params.put("interests", profile.getInterests());
        // location
        params.put("neighborhood", clientAddress.getNeighborhood());
        params.put("city", clientAddress.getCity());
        params.put("state", clientAddress.getState());
        params.put("country", clientAddress.getCountry());
        params.put("latitude", clientAddress.getLatitude());
        params.put("longitude", clientAddress.getLongitude());


        try {
            JSONObject response = APIClient.post("/ad", params, headers);

            return new Ad(response.getJSONObject("ad"));

        } catch (JSONException | IOException e) {
            // TODO: handle it and delivery a code
            throw new AdRequestException(AdRequestException.INTERNAL_ERROR);
        }

    }

    public void sendEvent(Ad ad, int eventType) {
        new Thread(() -> {

                try {
                    HashMap<String, String> headers = new HashMap();
                    headers.put("client_key", AdServer.getClientKey());

                    HashMap params = new HashMap<String, String>();
                    params.put("id", ad.getId());
                    params.put("event_type", eventType);

                    APIClient.post("/event", params, headers);
                } catch (JSONException | IOException e) {
                    // TODO: handle it and delivery a code?

                }

        }).start();


    }

    public static ClientAddress getAddress(Location location) throws AdRequestException {

        HashMap<String, String> headers = new HashMap();
        headers.put("client_key", AdServer.getClientKey());

        HashMap params = new HashMap<String, String>();

        if (location != null) {
            params.put("latitude", location.getLatitude());
            params.put("longitude", location.getLongitude());
        }


        try {
            JSONObject response = APIClient.get("address", params, headers);
            Log.d("TAG", "getAddressAEWWW: " + response.toString());

            return new ClientAddress(response.getJSONObject("address"));
        } catch (JSONException | IOException e) {
            Log.d("", "getAddress: " + e);
            // TODO: handle it and delivery a code
            throw new AdRequestException(AdRequestException.CLIENT_KEY_ERROR);
        }

    }


}


