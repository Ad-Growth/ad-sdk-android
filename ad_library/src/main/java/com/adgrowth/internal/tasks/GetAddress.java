package com.adgrowth.internal.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.internal.exceptions.APIIOException;
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

    private final OnStartCallback mCallback;
    private final APIClient mApiClient;
    private final ClientProfile mProfile;
    private SDKInitException mException;

    public GetAddress(OnStartCallback callback) {
        this.mProfile = AdServer.getUserProfile();
        this.mCallback = callback;
        this.mApiClient = new APIClient();
    }


    @Override
    protected ClientAddress doInBackground(String... strings) {

        try {
            double latitude = mProfile.getClientAddress().getLatitude();
            double longitude = mProfile.getClientAddress().getLongitude();

            ClientAddress clientAddress = getAddress(latitude, longitude);

            return clientAddress;
        } catch (AdRequestException e) {
            mException = new SDKInitException(e);
            return null;
        }

    }

    public ClientAddress getAddress(Double latitude, Double longitude) throws AdRequestException {


        HashMap<String, Object> params = new HashMap<>();

        if (latitude != 0.0 && longitude != 0.0) {
            params.put("latitude", latitude);
            params.put("longitude", longitude);
        } else {
            params.put("latitude", "");
            params.put("longitude", "");
        }

        try {

            JSONObject response = mApiClient.get("/functions/geolocate", params);

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

        if (mException != null) {
            mCallback.onFailed(mException);
            return;
        }

        mCallback.onInit(clientAddress);
    }

    public interface OnStartCallback {
        void onInit(ClientAddress clientAddress);

        void onFailed(SDKInitException e);
    }
}
