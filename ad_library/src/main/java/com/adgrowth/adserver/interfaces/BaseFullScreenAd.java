package com.adgrowth.adserver.interfaces;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adgrowth.adserver.R;
import com.adgrowth.adserver.constants.AdEventType;
import com.adgrowth.adserver.constants.AdMediaType;
import com.adgrowth.adserver.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.http.AdRequest;
import com.adgrowth.adserver.views.AdDialog;
import com.adgrowth.adserver.views.AdImageView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
public abstract class BaseFullScreenAd implements Application.ActivityLifecycleCallbacks, DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected ImageView imageView;
    protected InterstitialAdListener listener;
    protected AdRequest adRequest;
    protected Ad ad;
    protected Timer timer;
    protected Timer countdownTimer;
    protected Activity context;
    protected AdDialog dialog;
    protected ExoPlayer player;
    protected boolean mediaIsReady = false;

    protected StyledPlayerView playerView;
    protected View.OnClickListener onAdClickListener;
    private int countdown;

    public abstract void show(Activity context);


    public abstract void load(Context context);


    protected void prepareDialog() {
        dialog = new AdDialog(this.context);
        dialog.setOnShowListener(this);
        dialog.setOnDismissListener(this);
        dialog.setOnCloseListener(onCloseListener);

        if (ad.getMediaType() == AdMediaType.VIDEO) {
            long duration = getAdDuration();
//            player.createMessage((messageType, payload) -> dialog.showCloseButton())
//                    .setLooper(Looper.getMainLooper())
//                    .setPosition(0, 5_000)
//                    .setDeleteAfterDelivery(false)
//                    .send();
            player.createMessage((messageType, payload) -> presentPostAd())
                    .setLooper(Looper.getMainLooper())
                    .setPosition(0, duration)
                    .setDeleteAfterDelivery(false)
                    .send();
        }
    }

    protected void startShowCloseButtonCountdown() {
        countdownTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                countdown++;
                Log.d("TAG", "run: TIMER " + countdown);
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
        if (timer != null)
            timer.cancel();
        if (player != null) {
            player.removeListener(playerListener);
            player.release();
        }

        if (this.listener != null) {
            this.listener.onDismissed();
        }

        dialog.dismiss();
        dialog = null;
        adRequest.sendEvent(ad, AdEventType.DISMISSED);
    }

    private void presentPostAd() {
        playerView.setVisibility(View.GONE);
        dialog.hideProgressBar();
        player.stop();
        player.release();
        player = null;
        imageView = new AdImageView(this.context, ad.getPostMediaUrl(), onAdClickListener);
        imageView.setVisibility(View.VISIBLE);

        ((LinearLayout) dialog.findViewById(R.id.content_container)).addView(imageView);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        if (this.listener != null) {
            this.listener.onImpression();
        }

        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startShowCloseButtonCountdown();


        ad.setConsumed(true);

        adRequest.sendEvent(ad, AdEventType.PRINTED);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        dismiss();
    }

    protected View.OnClickListener onCloseListener = view -> dismiss();


    protected long getAdDuration() {
        int duration = 30_000;

        if ((int) (player.getDuration() / 1000) <= 30) {
            duration = (int) player.getDuration();
        }

        return duration;
    }

    protected final Player.Listener playerListener = new Player.Listener() {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_READY) {
                if (!player.isPlaying()) {
                    mediaIsReady = true;
                    listener.onLoad();
                }
            }

        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            if (!mediaIsReady)
                listener.onFailedToLoad(new AdRequestException(AdRequestException.PLAYBACK_ERROR));
            else
                listener.onFailedToShow(Ad.MEDIA_ERROR);

        }


        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Player.Listener.super.onIsPlayingChanged(isPlaying);
            if (isPlaying) {
                if (timer != null) timer.cancel();
                timer = new Timer();

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        new Handler(player.getApplicationLooper()).post(() -> {
                                    long duration = getAdDuration() / 1000;
                                    dialog.setVideoProgress((int) (((player.getCurrentPosition() / 10) / duration) + 1));
                                }
                        );
                    }
                };

                timer.scheduleAtFixedRate(task, 0, 1000);
                return;
            }

            timer.cancel();
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
        if (player != null) {
            player.play();
            return;
        }
        if (countdown < 5)
            startShowCloseButtonCountdown();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (player != null) {
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
