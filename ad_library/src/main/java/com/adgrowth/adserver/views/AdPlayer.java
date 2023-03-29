package com.adgrowth.adserver.views;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AdPlayer extends TextureView
        implements
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    MediaPlayer mediaPlayer;
    private int currentPosition = 0;
    private boolean isPaused;
    private Listener listener;
    private Timer timer;

    public AdPlayer(Activity context, String url, Listener playerListener) {

        super(context);
        mediaPlayer = new MediaPlayer();
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        this.listener = playerListener;
        setSurfaceTextureListener(this);

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);

            mediaPlayer.setOnCompletionListener(this);

            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

    void trackProgress() {
        Log.d("TAG", "trackProgress: ");
        stopTrackingProgress();
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (listener != null) ((Activity) getContext()).runOnUiThread(() -> {
                    try {
                        long duration = getAdDuration() / 1000;
                        int position = (mediaPlayer.getCurrentPosition() / 10);

                        listener.onProgress((int) ((position / duration) + 1));
                    } catch (IllegalStateException | NullPointerException ignored) {
                    }

                });

            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    long getAdDuration() {
        int duration = 30_000;
        if ((int) (mediaPlayer.getDuration() / 1000) <= 30)
            duration = (int) mediaPlayer.getDuration();
        return duration;
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();

        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {

            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {

            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }

        int xOffset = (viewWidth - newWidth) / 2;
        int yOffset = (viewHeight - newHeight) / 2;

        Matrix matrix = new Matrix();
        this.getTransform(matrix);
        matrix.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        matrix.postTranslate(xOffset, yOffset);
        this.setTransform(matrix);
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    public void play() {
        mediaPlayer.seekTo(currentPosition);
        trackProgress();
        mediaPlayer.start();
        isPaused = false;
        if (listener != null) listener.onPlay();
    }

    public void pause() {
        try {
            stopTrackingProgress();
            isPaused = true;
            if (listener != null) listener.onPause();

            currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        } catch (IllegalStateException ignored) {
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopTrackingProgress();
        if (listener != null) listener.onFinish();
    }

    private void stopTrackingProgress() {
        try {
            if (timer != null) timer.cancel();
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        // TODO: handle?
        if (listener != null) listener.onError();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        adjustAspectRatio(mp.getVideoWidth(), mp.getVideoHeight());

        if (listener != null) listener.onReady();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("TAG", "onSurfaceTextureAvailable: " + i);
        mediaPlayer.setSurface(new Surface(surfaceTexture));
        play();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
    }


    public interface Listener {
        void onProgress(int progress);

        void onReady();

        default void onPause() {

        }

        default void onPlay() {

        }

        abstract void onFinish();

        abstract void onError();

    }
}
