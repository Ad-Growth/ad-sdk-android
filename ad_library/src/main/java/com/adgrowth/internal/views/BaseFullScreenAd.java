package com.adgrowth.internal.views;

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

import com.adgrowth.adserver.R;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.interfaces.BaseAdListener;
import com.adgrowth.internal.entities.Ad;
import com.adgrowth.internal.enums.AdMediaType;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.helpers.FullScreenEventManager;
import com.adgrowth.internal.helpers.ScreenHelpers;
import com.adgrowth.internal.http.AdRequest;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseFullScreenAd<Listener extends BaseAdListener> implements Application.ActivityLifecycleCallbacks, DialogInterface.OnShowListener, DialogInterface.OnDismissListener, AdPlayer.Listener, AdImage.Listener {
    private static final int DEFAULT_AD_DURATION = 30;
    protected static final int TIME_TO_CLOSE = 5;
    protected AdImage mAdImage;
    protected Listener mListener;
    protected AdRequest mAdRequest;
    protected Ad mAd;
    protected Timer mAdDisplayTimer = new Timer();
    protected int mCurrentAdDisplayTime = 0;
    protected int mAdDuration = DEFAULT_AD_DURATION;
    protected Activity mContext;
    protected AdDialog mDialog;
    protected boolean mVideoIsReady = false;
    protected boolean mImageIsReady = false;
    protected boolean mAdIsReady = false;
    protected boolean mFailedToLoad = false;
    protected LinearLayout mAdContainerView;
    protected AdPlayer mPlayer;
    protected View.OnClickListener mOnAdClickListener = view -> {

        if (view.isEnabled()) {

            mAdRequest.sendClick(mContext, mAd);
            if (mListener != null) mListener.onClicked();

        }
    };
    protected View.OnClickListener onCloseListener = view -> dismiss();


    protected void requestAd(Activity context, AdType adType) {
        this.mContext = context;
        mFailedToLoad = false;

        if (mAd != null) {
            context.runOnUiThread(() -> mListener.onFailedToLoad(new AdRequestException(AdRequestException.ALREADY_LOADED)));
            return;
        }

        (new Thread(() -> {
            try {
                HashMap<String, Object> options = new HashMap<>();

                options.put("orientation", ScreenHelpers.getOrientation(context));

                mAd = mAdRequest.getAd(options);

                if (mAd.getType() != adType) {
                    throw new AdRequestException(AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE);
                }

                AdMediaType mediaType = mAd.getMediaType();

                if (mediaType == AdMediaType.VIDEO) {
                    mPlayer = new AdPlayer(context, mAd.getMediaUrl(), this);
                    mPlayer.setOnClickListener(mOnAdClickListener);
                }

                String imageUrl = mAd.getPostMediaUrl();

                if (mediaType == AdMediaType.IMAGE) {
                    imageUrl = mAd.getMediaUrl();
                }

                mAdImage = new AdImage(context, imageUrl, this);
                mAdImage.setOnClickListener(mOnAdClickListener);


            } catch (AdRequestException e) {
                context.runOnUiThread(() -> mListener.onFailedToLoad(e));
            }

        })).start();

    }

    public void show(Activity context) {

        if (mAd == null || !mAdIsReady) {
            mListener.onFailedToShow(Ad.NOT_READY);
            return;
        }

        if (mAd.isConsumed()) {
            mListener.onFailedToShow(Ad.ALREADY_CONSUMED);
            return;
        }

        if (!FullScreenEventManager.getShowPermission()) {
            mListener.onFailedToShow(Ad.ALREADY_SHOWING_FULL_SCREEN_AD);
            return;
        }

        this.mContext = context;
        AdMediaType type = mAd.getMediaType();

        prepareDialog();

        if (type == AdMediaType.IMAGE) mAdContainerView.addView(mAdImage);

        if (type == AdMediaType.VIDEO) mAdContainerView.addView(mPlayer);
        mDialog.show();
    }

    protected void prepareDialog() {
        mDialog = new AdDialog(this.mContext);
        mDialog.setOnShowListener(this);
        mDialog.setOnDismissListener(this);
        mDialog.setOnCloseListener(onCloseListener);
        mAdContainerView = (mDialog.findViewById(R.id.content_container));
        mAdContainerView.setOnClickListener(mOnAdClickListener);
    }

    protected void presentPostAd() {
        mPlayer.setVisibility(View.GONE);
        mPlayer.release();
        mDialog.hideProgressBar();

        mAdContainerView.addView(mAdImage);
        mAdContainerView.removeView(mPlayer);
        mAdImage.setVisibility(View.VISIBLE);
    }


    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    protected void dismiss() {
        mDialog.dismiss();
    }

    public boolean isLoaded() {
        return mAdIsReady;
    }

    public boolean isFailed() {
        return mFailedToLoad;
    }

    protected void startAdDisplayTimer() {
        if (mAdDisplayTimer != null) mAdDisplayTimer.cancel();

        mAdDisplayTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mCurrentAdDisplayTime++;
                mContext.runOnUiThread(() -> onDisplayTimeChanged(mCurrentAdDisplayTime));
            }
        };

        mAdDisplayTimer.scheduleAtFixedRate(task, 1000, 1000);
    }

    protected void stopAdStartedTimer() {
        if (mAdDisplayTimer != null) {
            mAdDisplayTimer.cancel();
            mAdDisplayTimer = null;
        }
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        mContext.runOnUiThread(() -> this.mListener.onImpression());
        mContext.getApplication().registerActivityLifecycleCallbacks(this);

        (mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        Objects.requireNonNull(mDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mPlayer != null && mAd.getMediaType() == AdMediaType.VIDEO) {
            mPlayer.play();
        }

        startAdDisplayTimer();


        mAd.setConsumed(true);
        FullScreenEventManager.notifyFullScreenShown(hashCode());
        mAdRequest.sendImpression(mContext, mAd);
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        Objects.requireNonNull(mDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext.getApplication().unregisterActivityLifecycleCallbacks(this);
        (mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (mPlayer != null) mPlayer.release();
        FullScreenEventManager.notifyFullScreenDismissed();
        stopAdStartedTimer();
    }


    @Override
    public void onImageReady() {

        mImageIsReady = true;
        if (mAd.getMediaType() == AdMediaType.VIDEO) {
            if (mVideoIsReady) mAdIsReady = true;
        } else mAdIsReady = true;

        if (mAdIsReady) mContext.runOnUiThread(() -> mListener.onLoad(this));
    }

    @Override
    public void onImageError() {
        mFailedToLoad = true;
        mListener.onFailedToLoad(new AdRequestException(AdRequestException.NETWORK_ERROR));
    }

    @Override
    public void onVideoReady(int videoDuration) {
        mAdDuration = videoDuration;
        mVideoIsReady = true;
        if (mImageIsReady) mAdIsReady = true;

        if (mAdIsReady) mListener.onLoad(this);
    }

    @Override
    public void onVideoFinished() {
        presentPostAd();
    }

    @Override
    public void onVideoError() {
        mListener.onFailedToLoad(new AdRequestException(AdRequestException.PLAYBACK_ERROR));
    }


    @Override
    public void onVideoProgressChanged(double currentPosition, double duration) {
        mDialog.setVideoProgress((int) ((currentPosition / duration) * 100));
    }


    protected void onDisplayTimeChanged(int adStartedTime) {
        if (adStartedTime >= TIME_TO_CLOSE && !mDialog.isCloseButtonEnabled())
            mDialog.enableCloseButton();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startAdDisplayTimer();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        startAdDisplayTimer();
        if (mPlayer != null && mAd.getMediaType() == AdMediaType.VIDEO) {
            mPlayer.play();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

        stopAdStartedTimer();

        if (mPlayer != null && mAd.getMediaType() == AdMediaType.VIDEO) {
            mPlayer.pause();
        }

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        stopAdStartedTimer();
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        stopAdStartedTimer();
    }

}
