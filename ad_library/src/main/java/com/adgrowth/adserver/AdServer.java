package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;

import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.adserver.exceptions.SDKInitException;
import com.adgrowth.adserver.helpers.GeolocationHelpers;
import com.adgrowth.adserver.interfaces.OnInitSDKCallback;
import com.adgrowth.adserver.interfaces.OnStartCallback;
import com.adgrowth.adserver.tasks.StartSdk;

public class AdServer {
    private static final int MIN_TIME_MS = 100;
    private static final int MIN_DISTANCE_METERS = 0;
    private static String clientKey;
    private static ClientAddress clientAddress;
    private static ClientProfile clientProfile;
    private static Context context;
    private static OnInitSDKCallback callback;
    private static Location location;

    public static String getClientKey() {
        return clientKey;
    }

    public static void init(Context context, String key, OnInitSDKCallback callback) {
        AdServer.callback = callback;
        AdServer.context = context;
        AdServer.clientProfile = new ClientProfile();

        if (key.equals(clientKey)) return;

        AdServer.clientKey = key;
        checkLocation();
    }

    public static void init(Context context, String key, ClientProfile profile, OnInitSDKCallback callback) {
        AdServer.clientProfile = profile;
        AdServer.context = context;
        AdServer.callback = callback;

        if (key.equals(clientKey)) return;

        AdServer.clientKey = key;
        checkLocation();
    }

    @SuppressLint("MissingPermission")
    private static void checkLocation() {
        // get geolocation if available
        if (GeolocationHelpers.checkLocationPermission(context)) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_METERS, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull android.location.Location l) {
                    AdServer.location = l;
                    locationManager.removeUpdates(this);
                    startSDK();
                }
            });

            return;
        }

        // continue without geolocation
        startSDK();


    }

    private static void startSDK() {
        new StartSdk(location, new OnStartCallback() {
            @Override
            public void onInit(ClientAddress l) {
                AdServer.clientAddress = l;
                AdServer.callback.onInit();
            }

            @Override
            public void onFailed(SDKInitException e) {
                AdServer.callback.onFailed(e);
            }
        }).execute();
    }

    public static ClientAddress getLocation() {
        return AdServer.clientAddress;
    }

    public static void setUserProfile(ClientProfile profile) {
        AdServer.clientProfile = profile;
    }

    public static ClientProfile getUserProfile() {
        return AdServer.clientProfile;
    }
}
