package com.adgrowth.adserver;

import android.annotation.SuppressLint;

import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.adserver.exceptions.SDKInitException;
import com.adgrowth.internal.tasks.GetAddress;

public class AdServer {

    private static String clientKey;
    private static ClientProfile clientProfile = new ClientProfile();

    private static Listener callback;
    private static Boolean initialized = false;

    public static String getClientKey() {
        return clientKey;
    }

    public static void initialize(String key, Listener callback) {
        AdServer.callback = callback;
        AdServer.clientProfile = new ClientProfile();

        if (key.equals(clientKey)) return;

        AdServer.clientKey = key;
        startSDK();
    }

    public static void initialize(String key, ClientProfile profile, Listener callback) {
        if (initialized) {
            callback.onFailed(new SDKInitException(SDKInitException.ALREADY_INITIALIZED));
            return;
        }
        AdServer.clientProfile = profile;

        AdServer.callback = callback;

        if (key.equals(clientKey)) {
            callback.onInit();
            initialized = true;
            return;
        }

        AdServer.clientKey = key;
        startSDK();
    }

    public static Boolean isInitialized() {
        return initialized;
    }

    public static void finish() {
        AdServer.clientKey = null;
        AdServer.clientProfile = null;


        AdServer.initialized = false;
    }


    @SuppressLint("NewApi")
    private static void startSDK() {
        new GetAddress(new GetAddress.OnStartCallback() {
            @Override
            public void onInit(ClientAddress l) {
                AdServer.clientProfile.setClientAddress(l);
                AdServer.callback.onInit();
            }
            @Override
            public void onFailed(SDKInitException e) {
                AdServer.callback.onFailed(e);
            }
        }).execute();
    }


    public static void setUserProfile(ClientProfile profile) {
        AdServer.clientProfile = profile;
    }

    public static ClientProfile getUserProfile() {
        return AdServer.clientProfile;
    }


    public interface Listener {
        void onInit();

        void onFailed(SDKInitException e);
    }
}
