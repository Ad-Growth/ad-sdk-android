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

    MediaPlayer mMediaPlayer;
    private int mCurrentPosition = 0;
    private Listener mListener;
    private Timer mTimer;
    private boolean mReleased = false;
    private int mAdDuration = 30;

    public AdPlayer(Activity context, String url, Listener playerListener) {

        super(context);
        mMediaPlayer = new MediaPlayer();
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        this.mListener = playerListener;
        setSurfaceTextureListener(this);

        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);

            mMediaPlayer.setOnCompletionListener(this);

            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

    void trackProgress() {
        if (mReleased || mMediaPlayer == null) return;
        stopTrackingProgress();
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mListener != null) ((Activity) getContext()).runOnUiThread(() -> {
                    try {
                        mListener.onVideoProgressChanged(
                                (double) (mMediaPlayer.getCurrentPosition() / 1000),
                                (double) (mAdDuration));
                    } catch (Exception ignored) {
                    }
                });

            }
        };

        mTimer.scheduleAtFixedRate(task, 0, 500);
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
        if (mReleased || mMediaPlayer == null) return;

        mMediaPlayer.reset();
        mMediaPlayer.release();
        mReleased = true;
        mMediaPlayer = null;
    }

    public void play() {
        if (mReleased || mMediaPlayer == null) return;
        mMediaPlayer.seekTo(mCurrentPosition);
        trackProgress();
        mMediaPlayer.start();

        if (mListener != null) mListener.onPlay();
    }

    public void pause() {
        if (mReleased || mMediaPlayer == null) return;
        try {

            if (mListener != null) mListener.onPause();

            mCurrentPosition = mMediaPlayer.getCurrentPosition();
            stopTrackingProgress();
            mMediaPlayer.pause();
        } catch (IllegalStateException ignored) {
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopTrackingProgress();
        if (mListener != null) mListener.onVideoFinished();
    }

    private void stopTrackingProgress() {
        try {
            if (mTimer != null) mTimer.cancel();
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        if (mListener != null) mListener.onVideoError();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        adjustAspectRatio(mp.getVideoWidth(), mp.getVideoHeight());
        mAdDuration = (int) Math.ceil((double) (mp.getDuration() / 1000));

        if (mListener != null) mListener.onVideoReady(mAdDuration);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mReleased || mMediaPlayer == null) return;
        adjustAspectRatio(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mReleased || mMediaPlayer == null) return;
        mMediaPlayer.setSurface(new Surface(surfaceTexture));
        play();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        if (mReleased || mMediaPlayer == null) return;
        adjustAspectRatio(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
    }


    public interface Listener {

        void onVideoProgressChanged(double position, double total);

        void onVideoReady(int videoDuration);

        default void onPause() {

        }

        default void onPlay() {

        }

        abstract void onVideoFinished();

        abstract void onVideoError();

    }
}
