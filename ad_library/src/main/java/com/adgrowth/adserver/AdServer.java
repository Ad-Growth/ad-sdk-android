package com.adgrowth.adserver;

import android.content.Context;

import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.adserver.exceptions.SDKInitException;
import com.adgrowth.internal.helpers.AdvertisingIdManager;
import com.adgrowth.internal.tasks.GetAddress;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class AdServer {

    private static String mClientKey;
    private static AdvertisingIdManager mAdvertisingIdManager;
    private static String mAdId;
    private static ClientProfile mClientProfile = new ClientProfile();
    private static Listener mCallback;
    private static Boolean mInitialized = false;


    public static void initialize(Context context, String clientKey, Listener callback) {
        AdServer.mCallback = callback;
        AdServer.mClientProfile = new ClientProfile();
        if (clientKey.equals(mClientKey)) return;

        AdServer.mAdvertisingIdManager = new AdvertisingIdManager(context);
        AdServer.mClientKey = clientKey;
        startSDK();
    }

    public static void initialize(Context context, String clientKey, ClientProfile profile, Listener callback) {
        if (mInitialized) {
            callback.onFailed(new SDKInitException(SDKInitException.ALREADY_INITIALIZED));
            return;
        }

        AdServer.mCallback = callback;

        if (clientKey.equals(mClientKey)) {
            mInitialized = true;
            callback.onInit();
            return;
        }

        AdServer.mClientProfile = profile;
        AdServer.mAdvertisingIdManager = new AdvertisingIdManager(context);
        AdServer.mClientKey = clientKey;
        startSDK();
    }

    public static Boolean isInitialized() {
        return mInitialized;
    }

    public static void finish() {
        AdServer.mClientKey = null;
        AdServer.mClientProfile = new ClientProfile();
        AdServer.mInitialized = false;
    }


    private static void startSDK() {

        new Thread(() -> {
            try {
                mAdId = mAdvertisingIdManager.getAdvertisingId();
            } catch (IOException | GooglePlayServicesNotAvailableException |
                     GooglePlayServicesRepairableException e) {
                mAdId = mAdvertisingIdManager.getUniqueId();
            }
        }).start();

        new GetAddress(new GetAddress.OnStartCallback() {
            @Override
            public void onInit(ClientAddress l) {
                mInitialized = true;
                AdServer.mClientProfile.setClientAddress(l);
                AdServer.mCallback.onInit();
            }

            @Override
            public void onFailed(SDKInitException e) {
                AdServer.mCallback.onFailed(e);
            }
        }).execute();
    }


    public static void setClientProfile(ClientProfile profile) {
        AdServer.mClientProfile = profile;
    }


    public static String getClientKey() {
        return mClientKey;
    }

    public static ClientProfile getUserProfile() {
        return AdServer.mClientProfile;
    }

    public static String getAdId() {
        return mAdId;
    }


    public interface Listener {
        void onInit();

        void onFailed(SDKInitException e);
    }
}
