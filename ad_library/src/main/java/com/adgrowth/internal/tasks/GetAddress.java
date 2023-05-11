package com.adgrowth.internal.tasks;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.adserver.exceptions.APIIOException;
import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.internal.helpers.IOErrorHandler;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.exceptions.SDKInitException;
import com.adgrowth.internal.http.APIClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


@SuppressLint("NewApi")
public class GetAddress extends AsyncTask<String, String, ClientAddress> {

    private final OnStartCallback callback;
    private final APIClient apiClient;
    private final ClientProfile profile;

    private SDKInitException exception;

    public GetAddress(OnStartCallback callback) {
        this.profile = AdServer.getUserProfile();
        this.callback = callback;
        this.apiClient = new APIClient();
    }


    @Override
    protected ClientAddress doInBackground(String... strings) {

        try {
            double latitude = profile.getClientAddress().getLatitude();
            double longitude = profile.getClientAddress().getLongitude();

            ClientAddress clientAddress = getAddress(latitude, longitude);

            return clientAddress;
        } catch (AdRequestException e) {
            exception = new SDKInitException(e);
            return null;
        }

    }

    public ClientAddress getAddress(@Nullable Double latitude, @Nullable Double longitude) throws AdRequestException {


        HashMap params = new HashMap<String, String>();

        if (latitude != 0.0 && longitude != 0.0) {
            params.put("latitude", latitude);
            params.put("longitude", longitude);
        } else {
            params.put("latitude", "");
            params.put("longitude", "");
        }

        try {

            JSONObject response = apiClient.get("/localization/geolocate", params);

            return new ClientAddress(response.getJSONObject("localization"));

        } catch (APIIOException | JSONException e) {
            // fallback
            if (e.getMessage().contains("Invalid latitude/longitude"))
                return new ClientAddress(new JSONObject());


            throw IOErrorHandler.handle(e);
        }

    }

    @Override
    protected void onPostExecute(ClientAddress clientAddress) {

        if (exception != null) {
            callback.onFailed(exception);
            return;
        }

        callback.onInit(clientAddress);
    }

    public interface OnStartCallback {
        void onInit(ClientAddress clientAddress);

        void onFailed(SDKInitException e);
    }
}
