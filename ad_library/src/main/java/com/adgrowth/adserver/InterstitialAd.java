package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;

import com.adgrowth.adserver.constants.AdMediaType;
import com.adgrowth.adserver.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.http.AdRequest;
import com.adgrowth.adserver.views.AdImage;
import com.adgrowth.adserver.views.AdPlayer;

public class InterstitialAd extends BaseFullScreenAd {

    private String unitId;

    public InterstitialAd(String unitId, AdRequest adRequest) {

        this.unitId = unitId;
        this.adRequest = adRequest;

    }


    @SuppressLint("NewApi")
    @Override
    public void show(Activity context) {

        if (ad == null || !mediaIsReady) {
            listener.onFailedToShow(Ad.NOT_READY);
            return;
        }
        if (ad.isConsumed()) {
            listener.onFailedToShow(Ad.ALREADY_CONSUMED);
            return;
        }

        this.context = context;
        AdMediaType type = ad.getMediaType();

        prepareDialog();

        if (type == AdMediaType.IMAGE)
            container.addView(imageView);

        if (type == AdMediaType.VIDEO)
            container.addView(player);


        dialog.show();
    }

    @Override
    public void load(Activity context) {
        this.context = context;

        if (ad != null) {
            listener.onFailedToLoad(new AdRequestException(AdRequestException.ALREADY_LOADED));
            return;
        }

        new Thread(() -> {
            try {
                ad = adRequest.getAd(unitId);
                Log.d("TAG", "load: ad"+ad);
                AdMediaType type = ad.getMediaType();

                if (type == AdMediaType.IMAGE) {
                    imageView = new AdImage(context, ad.getMediaUrl(), imageListener);
                    imageView.setOnClickListener(onAdClickListener);

                }

                if (type == AdMediaType.VIDEO) {
                    player = new AdPlayer(context, ad.getMediaUrl(), playerListener);
                    player.setOnClickListener(onAdClickListener);
                }


            } catch (AdRequestException e) {

                listener.onFailedToLoad(e);
            }

        }).start();

    }


}