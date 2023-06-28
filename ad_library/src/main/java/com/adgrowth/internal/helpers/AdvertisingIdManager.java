package com.adgrowth.internal.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.UUID;

public class AdvertisingIdManager {

    private static final String UNIQUE_ID_KEY = "unique_id";

    private final Context mContext;
    private final SharedPreferences mPreferences;

    public AdvertisingIdManager(Context context) {
        this.mContext = context.getApplicationContext();
        mPreferences = this.mContext.getSharedPreferences("advertising_id", Context.MODE_PRIVATE);
    }

    public String getAdvertisingId() throws IOException, GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {

        AdvertisingIdClient.Info advertisingIdInfoListenableFuture = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
        return advertisingIdInfoListenableFuture.getId();

    }

    public String getUniqueId() {
        String uniqueId = mPreferences.getString(UNIQUE_ID_KEY, null);

        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
            mPreferences.edit().putString(UNIQUE_ID_KEY, uniqueId).apply();
        }
        return uniqueId;
    }
}