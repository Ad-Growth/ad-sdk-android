package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.adgrowth.internal.enums.AdMediaType;
import com.adgrowth.internal.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.interfaces.BaseAdListener;
import com.adgrowth.internal.views.AdImage;
import com.adgrowth.internal.views.AdPlayer;

public class InterstitialAd extends BaseFullScreenAd {

    private String unitId;

    public InterstitialAd(String unitId, AdRequest adRequest) {

        this.unitId = unitId;
        this.adRequest = adRequest;

    }

    public String getUnitId() {
        return unitId;
    }

    @SuppressLint("NewApi")
    @Override
    public void show(Activity context) {

        if (ad == null || !adIsReady) {
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
            container.addView(adImage);

        if (type == AdMediaType.VIDEO)
            container.addView(player);


        dialog.show();
    }

    @Override
    public void load(Activity context) {
        this.context = context;

        if (ad != null) {
            context.runOnUiThread(() -> listener.onFailedToLoad(new AdRequestException(AdRequestException.ALREADY_LOADED)));
            return;
        }

        new Thread(() -> {
            try {
                ad = adRequest.getAd(unitId, null);
                AdMediaType type = ad.getMediaType();


                if (type == AdMediaType.VIDEO) {
                    player = new AdPlayer(context, ad.getMediaUrl(), playerListener);
                    player.setOnClickListener(onAdClickListener);
                }


                String imageUrl = ad.getPostMediaUrl();

                if (type == AdMediaType.IMAGE) {
                    imageUrl = ad.getMediaUrl();
                }

                adImage = new AdImage(context, imageUrl, imageListener);
                adImage.setOnClickListener(onAdClickListener);


            } catch (AdRequestException e) {
                context.runOnUiThread(() ->
                        listener.onFailedToLoad(e)
                );
            }

        }).start();

    }


    public abstract static class Listener extends BaseAdListener {
        public void onDismissed() {
        }

    }
}