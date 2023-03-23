package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;

import com.adgrowth.adserver.constants.AdEventType;
import com.adgrowth.adserver.constants.AdMediaType;
import com.adgrowth.adserver.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.helpers.OnClickHelpers;
import com.adgrowth.adserver.http.AdRequest;
import com.adgrowth.adserver.interfaces.BaseFullScreenAd;
import com.adgrowth.adserver.views.AdImageView;
import com.adgrowth.adserver.views.AdPlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

public class InterstitialAd extends BaseFullScreenAd {


    private String unitId;

    public InterstitialAd(String unitId, AdRequest adRequest) {

        this.unitId = unitId;
        this.adRequest = adRequest;

        this.onAdClickListener = view -> {

            if (player != null) player.pause();

            OnClickHelpers.openUrl(context, ad.getActionUrl());

            if (listener != null) listener.onClicked();

            adRequest.sendEvent(ad, AdEventType.CLICKED);
        };

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
        context.getApplication().registerActivityLifecycleCallbacks(this);

        prepareDialog();

        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.content_container);
        container.setOnClickListener(onAdClickListener);


        if (type == AdMediaType.IMAGE) {
            imageView = new AdImageView(context, ad.getMediaUrl(), onAdClickListener);
            container.addView(imageView);
            dialog.show();
            return;
        }


        new Handler(player.getApplicationLooper()).post(() -> {
            playerView = new AdPlayerView(context, player);
            container.addView(playerView);
        });

        dialog.show();
        player.play();
    }

    @Override
    public void load(Context context) {
        if (ad != null) {
            listener.onFailedToLoad(new AdRequestException(AdRequestException.ALREADY_LOADED));
            return;
        }
        new Thread(() -> {
            try {
                ad = adRequest.getAd(unitId);

                AdMediaType type = ad.getMediaType();
                Log.d("TAG", "load: AD: "+ad);
                if (type == AdMediaType.IMAGE) {
                    mediaIsReady = true;
                    this.listener.onLoad();
                    return;
                }

                player = new ExoPlayer.Builder(context).build();

                new Handler(player.getApplicationLooper()).post(() -> {
                    player.setMediaItem(MediaItem.fromUri(ad.getMediaUrl()));
                    player.addListener(playerListener);
                    player.prepare();
                });


            } catch (AdRequestException e) {
                listener.onFailedToLoad(e);
            }

        }).start();

    }


}