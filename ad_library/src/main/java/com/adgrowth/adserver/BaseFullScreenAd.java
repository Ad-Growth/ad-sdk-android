package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adgrowth.adserver.constants.AdEventType;
import com.adgrowth.adserver.constants.AdMediaType;
import com.adgrowth.adserver.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.helpers.OnClickHelpers;
import com.adgrowth.adserver.http.AdRequest;
import com.adgrowth.adserver.interfaces.InterstitialAdListener;
import com.adgrowth.adserver.views.AdDialog;
import com.adgrowth.adserver.views.AdImage;
import com.adgrowth.adserver.views.AdPlayer;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
abstract class BaseFullScreenAd implements Application.ActivityLifecycleCallbacks, DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected AdImage imageView;
    protected InterstitialAdListener listener;
    protected AdRequest adRequest;
    protected Ad ad;
    protected Timer countdownTimer = new Timer();
    protected Activity context;
    protected AdDialog dialog;

    protected boolean mediaIsReady = false;

    protected AdPlayer player;
    protected View.OnClickListener onAdClickListener = view -> {

        OnClickHelpers.openUrl(context, ad.getActionUrl());

        if (listener != null) listener.onClicked();

        adRequest.sendEvent(ad, AdEventType.CLICKED);
    };
    private int countdown;
    protected LinearLayout container;

    public abstract void show(Activity context);


    public abstract void load(Activity context);


    protected void prepareDialog() {
        dialog = new AdDialog(this.context);
        dialog.setOnShowListener(this);
        dialog.setOnDismissListener(this);
        dialog.setOnCloseListener(onCloseListener);
        container = ((LinearLayout) dialog.findViewById(R.id.content_container));
        container.setOnClickListener(onAdClickListener);
    }

    protected void startShowCloseButtonCountdown() {
        countdownTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                countdown++;
                if (countdown >= 5) {
                    context.runOnUiThread(() -> {
                        dialog.showCloseButton();
                        countdownTimer.cancel();
                        countdownTimer = null;

                    });
                }


            }
        };

        countdownTimer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void setListener(InterstitialAdListener listener) {
        this.listener = listener;
    }

    protected void dismiss() {

        context.runOnUiThread(() -> this.listener.onDismissed());
        dialog.dismiss();
//        TODO: may be used later
//        adRequest.sendEvent(ad, AdEventType.DISMISSED);
    }

    private void presentPostAd() {
        player.setVisibility(View.GONE);
        player.release();
        dialog.hideProgressBar();
        imageView = new AdImage(this.context, ad.getPostMediaUrl(), null);
        imageView.setVisibility(View.VISIBLE);
        container.addView(imageView);
        container.removeView(player);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        context.runOnUiThread(() -> this.listener.onImpression());
        context.getApplication().registerActivityLifecycleCallbacks(this);
        Objects.requireNonNull(dialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startShowCloseButtonCountdown();

        ad.setConsumed(true);
        adRequest.sendEvent(ad, AdEventType.PRINTED);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context.getApplication().unregisterActivityLifecycleCallbacks(this);
        dismiss();
    }

    protected View.OnClickListener onCloseListener = view -> dismiss();


    protected final AdImage.Listener imageListener = new AdImage.Listener() {
        @Override
        public void onLoad() {
            super.onLoad();
            mediaIsReady = true;
            context.runOnUiThread(() -> listener.onLoad());

        }

        @Override
        public void onLoadFailed(int code) {
            super.onLoadFailed(code);

            if (code == 1) listener.onFailedToShow(Ad.MEDIA_ERROR);
            else listener.onFailedToLoad(new AdRequestException(AdRequestException.NETWORK_ERROR));

        }
    };

    protected final AdPlayer.Listener playerListener = new AdPlayer.Listener() {
        @Override
        public void onReady() {
            mediaIsReady = true;
            listener.onLoad();
        }

        @Override
        public void onFinish() {
            presentPostAd();
        }

        @Override
        public void onError() {
            listener.onFailedToLoad(new AdRequestException(AdRequestException.PLAYBACK_ERROR));
        }


        @Override
        public void onProgress(int progress) {
            dialog.setVideoProgress(progress);
        }
    };

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (countdown < 5) startShowCloseButtonCountdown();
        if (player != null && ad.getMediaType() == AdMediaType.VIDEO) {
            player.play();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (player != null && ad.getMediaType() == AdMediaType.VIDEO) {
            player.pause();
            return;
        }

        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

}
