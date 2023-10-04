package com.adgrowth.adserver;

import android.app.Activity;

import com.adgrowth.adserver.interfaces.BaseAdListener;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.http.AdRequest;
import com.adgrowth.internal.views.BaseFullScreenAd;

public class InterstitialAd extends BaseFullScreenAd<InterstitialAd.Listener> {
    public InterstitialAd(String unitId) {
        this.mUnitId = unitId;
        this.mAdRequest = new AdRequest();
    }

    public void load(Activity context) {
        requestAd(context, AdType.INTERSTITIAL);
    }

    @Override
    protected void dismiss() {
        mContext.runOnUiThread(() -> mListener.onDismissed());
        super.dismiss();
    }


    public interface Listener extends BaseAdListener<InterstitialAd> {
        default void onDismissed() {

        }
    }
}
