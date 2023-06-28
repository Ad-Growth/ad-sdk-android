package com.adgrowth.adserver;

import android.app.Activity;

import com.adgrowth.internal.entities.Ad;
import com.adgrowth.internal.enums.AdMediaType;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.http.AdRequest;
import com.adgrowth.internal.interfaces.BaseAdListener;
import com.adgrowth.internal.views.BaseFullScreenAd;

public class InterstitialAd extends BaseFullScreenAd<InterstitialAd.Listener> {
    public InterstitialAd(String unitId) {
        this.mUnitId = unitId;
        this.mAdRequest = new AdRequest();
    }

    @Override
    public void show(Activity context) {

        if (mAd == null || !mAdIsReady) {
            mListener.onFailedToShow(Ad.NOT_READY);
            return;
        }

        if (mAd.isConsumed()) {
            mListener.onFailedToShow(Ad.ALREADY_CONSUMED);
            return;
        }

        this.mContext = context;
        AdMediaType type = mAd.getMediaType();

        prepareDialog();

        if (type == AdMediaType.IMAGE)
            mAdContainerView.addView(mAdImage);

        if (type == AdMediaType.VIDEO)
            mAdContainerView.addView(mPlayer);
        mDialog.show();
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
