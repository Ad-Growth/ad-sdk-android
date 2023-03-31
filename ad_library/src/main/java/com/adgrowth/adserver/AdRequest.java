package com.adgrowth.adserver;

import android.app.Activity;
import android.location.Location;

import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.internal.enums.AdEventType;

import com.adgrowth.internal.entities.Ad;
import com.adgrowth.internal.entities.ClientAddress;
import com.adgrowth.adserver.exceptions.APIIOException;
import com.adgrowth.adserver.exceptions.AdRequestException;
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

    public Ad getAd(String unitId) throws AdRequestException {

        HashMap systemInfo = SystemInfoHelpers.getSystemInfo(context);
        ClientProfile profile = AdServer.getUserProfile();
        ClientAddress clientAddress = AdServer.getLocation();

        HashMap<String, Object> params = new HashMap<>();

        params.put("unit_id", unitId);
//      TODO: send later
//      params.put("system_info", systemInfo);
        params.put("orientation", ScreenHelpers.getOrientation(context));

        params.put("age", (profile.getAge()));
        params.put("gender", profile.getGender());
        params.put("interests", profile.getInterests());

//        TODO: send later
//        params.put("neighborhood", clientAddress.getNeighborhood());
//        params.put("city", clientAddress.getCity());
//        params.put("state", clientAddress.getState());
//        params.put("country", clientAddress.getCountry());
//        params.put("latitude", clientAddress.getLatitude());
//        params.put("longitude", clientAddress.getLongitude());


        try {
            JSONObject response = apiClient.get("/campaigns/adverts/search", params);

            return new Ad(response.getJSONObject("adverts"));

        } catch (APIIOException | JSONException e) {

            if (e instanceof APIIOException) {
                switch (((APIIOException) e).getStatusCode()) {
                    case 404:
                        if (e.getMessage().contains("No ads found"))
                            throw new AdRequestException(AdRequestException.NO_AD_FOUND);
                        break;
                    case 400:
                        if (e.getMessage().contains("Unit_id invalid"))
                            throw new AdRequestException(AdRequestException.INVALID_UNIT_ID);

                        if (e.getMessage().contains("Client_key invalid"))
                            throw new AdRequestException(AdRequestException.INVALID_CLIENT_KEY);
                        break;
                }

            }
            throw new AdRequestException(AdRequestException.INTERNAL_ERROR);

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

    public static ClientAddress getAddress(Location location) throws AdRequestException {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("client_key", AdServer.getClientKey());

        HashMap params = new HashMap<String, String>();

        if (location != null) {
            params.put("latitude", location.getLatitude());
            params.put("longitude", location.getLongitude());
        }


        try {
            // TODO: unmock
//            JSONObject response = apiClient.get("/address", params, headers);
//            return new ClientAddress(response.getJSONObject("address"));

            return new ClientAddress(new JSONObject("{\"neighborhood\":\"Encruzilhada\",\"city\":\"Bom Jardim\",\"state\":\"PE\",\"country\":\"Brazil\",\"formatted_address\":\"59GX+72 Encruzilhada, Bom Jardim - PE, Brazil\",\"latitude\":-7.82434023,\"longitude\":-35.60242533}"));

        } catch (JSONException /*| IOException */ e) {
            e.printStackTrace();
            // TODO: handle it and delivery a code
            throw new AdRequestException(AdRequestException.INVALID_CLIENT_KEY);
        }

    }


}


