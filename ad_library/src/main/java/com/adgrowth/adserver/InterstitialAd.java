package com.adgrowth.adserver;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;

import com.adgrowth.adserver.constants.AdEventType;
import com.adgrowth.adserver.constants.AdMediaType;
import com.adgrowth.adserver.constants.AdType;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.helpers.OnClickHelpers;
import com.adgrowth.adserver.http.AdRequest;
import com.adgrowth.adserver.views.AdImageView;
import com.adgrowth.adserver.views.AdPlayerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

public class InterstitialAd extends FullScreenContent {


    private String unitId;

    public InterstitialAd(String unitId, AdRequest adRequest) {

        this.unitId = unitId;
        this.adRequest = adRequest;

        this.onAdClickListener = view -> {

            if (player != null) player.pause();

            OnClickHelpers.openUrl(context, ad.getActionUrl());

            if (callback != null) callback.onClicked();

            adRequest.sendEvent(ad, AdEventType.CLICKED);
        };

    }


    @Override
    public void show(Activity context) {
        this.context = context;
        int type = ad.getMediaType();
        context.getApplication().registerActivityLifecycleCallbacks(this);

        prepareDialog();


        LinearLayout container = dialog.findViewById(R.id.content_container);
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
        new Thread(() -> {
            try {
                ad = adRequest.getAd(AdType.INTERSTITIAL, unitId);
                Log.d("TAG", "load: "+ad);
                int type = ad.getMediaType();

                if (type == AdMediaType.IMAGE) {
                    this.callback.onLoad();
                    return;
                }

                player = new ExoPlayer.Builder(context).build();

                new Handler(player.getApplicationLooper()).post(() -> {
                    player.setMediaItem(MediaItem.fromUri(ad.getMediaUrl()));
                    player.addListener(playerListener);
                    player.prepare();
                });


            } catch (AdRequestException e) {
                callback.onFailedToLoad(e);
            }

        }).start();

    }

    @Override
    public void onReward(int reward) {

    }


}