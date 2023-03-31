package com.adgrowth.internal.views;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
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
    private boolean released = false;

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
        if (released || mediaPlayer == null) return;
        stopTrackingProgress();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (listener != null) ((Activity) getContext()).runOnUiThread(() -> {
                    try{

                    listener.onVideoProgressChanged(
                            (double) (mediaPlayer.getCurrentPosition() / 1000),
                            (double) (getAdDuration() / 1000));
                    } catch (Exception ignored) {
                    }
                });

            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    int getAdDuration() {
        if (released || mediaPlayer == null) return 30000;
        return mediaPlayer.getDuration();
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
        if (released || mediaPlayer == null) return;

        mediaPlayer.reset();
        mediaPlayer.release();
        released = true;
        mediaPlayer = null;
    }

    public void play() {
        if (released || mediaPlayer == null) return;
        mediaPlayer.seekTo(currentPosition);
        trackProgress();
        mediaPlayer.start();
        isPaused = false;
        if (listener != null) listener.onPlay();
    }

    public void pause() {
        if (released || mediaPlayer == null) return;
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
        if (released || mediaPlayer == null) return;
        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (released || mediaPlayer == null) return;
        mediaPlayer.setSurface(new Surface(surfaceTexture));
        play();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        if (released || mediaPlayer == null) return;
        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
    }


    public interface Listener {

        void onVideoProgressChanged(double position, double total);

        void onReady();

        default void onPause() {

        }

        default void onPlay() {

        }

        abstract void onFinish();

        abstract void onError();

    }
}
