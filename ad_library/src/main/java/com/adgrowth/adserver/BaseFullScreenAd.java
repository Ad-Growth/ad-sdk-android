package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adgrowth.internal.enums.AdEventType;
import com.adgrowth.internal.enums.AdMediaType;
import com.adgrowth.internal.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.helpers.OnClickHelpers;
import com.adgrowth.internal.views.AdDialog;
import com.adgrowth.internal.views.AdImage;
import com.adgrowth.internal.views.AdPlayer;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
abstract class BaseFullScreenAd implements Application.ActivityLifecycleCallbacks, DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected AdImage adImage;
    protected InterstitialAd.Listener listener;
    protected AdRequest adRequest;
    protected Ad ad;
    protected Timer countdownTimer = new Timer();
    protected Activity context;
    protected AdDialog dialog;
    private boolean videoIsReady = false;
    private boolean imageIsReady = false;
    protected boolean adIsReady = false;

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

    private void presentPostAd() {
        player.setVisibility(View.GONE);
        player.release();
        dialog.hideProgressBar();

        container.addView(adImage);
        container.removeView(player);
        adImage.setVisibility(View.VISIBLE);
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

    public void setListener(InterstitialAd.Listener listener) {
        this.listener = listener;
    }

    protected void dismiss() {

        context.runOnUiThread(() -> listener.onDismissed());
        dialog.dismiss();
//        TODO: may be used later
//        adRequest.sendEvent(ad, AdEventType.DISMISSED);
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        context.runOnUiThread(() -> this.listener.onImpression());
        context.getApplication().registerActivityLifecycleCallbacks(this);
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        Objects.requireNonNull(dialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startShowCloseButtonCountdown();
        ad.setConsumed(true);
        adRequest.sendEvent(ad, AdEventType.PRINTED);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context.getApplication().unregisterActivityLifecycleCallbacks(this);
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        dismiss();
    }

    protected View.OnClickListener onCloseListener = view -> dismiss();


    protected final AdImage.Listener imageListener = new AdImage.Listener() {
        @Override
        public void onLoad() {
            super.onLoad();
            imageIsReady = true;
            if (ad.getMediaType() == AdMediaType.VIDEO) {
                if (videoIsReady) adIsReady = true;
            } else {
                adIsReady = true;
            }

            if (adIsReady)
                context.runOnUiThread(() -> listener.onLoad());

        }

        @Override
        public void onLoadFailed() {
            super.onLoadFailed();
            listener.onFailedToLoad(new AdRequestException(AdRequestException.NETWORK_ERROR));

        }
    };


    protected final AdPlayer.Listener playerListener = new AdPlayer.Listener() {
        @Override
        public void onReady() {
            if (imageIsReady) adIsReady = true;

            if (adIsReady)
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
        public void onVideoProgressChanged(double currentPosition, double duration) {
            dialog.setVideoProgress((int) ((currentPosition / duration) * 100));
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
