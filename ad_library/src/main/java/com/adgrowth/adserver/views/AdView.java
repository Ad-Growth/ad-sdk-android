package com.adgrowth.adserver.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adgrowth.adserver.R;
import com.adgrowth.adserver.enums.AdOrientation;
import com.adgrowth.adserver.enums.AdSizeType;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.interfaces.BaseAdListener;
import com.adgrowth.internal.entities.Ad;
import com.adgrowth.internal.enums.AdMediaType;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.helpers.AdUriHelpers;
import com.adgrowth.internal.helpers.FullScreenEventManager;
import com.adgrowth.internal.http.AdRequest;
import com.adgrowth.internal.interfaces.FullScreenListener;
import com.adgrowth.internal.views.AdImage;
import com.adgrowth.internal.views.AdPlayer;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class AdView extends ViewGroup implements Application.ActivityLifecycleCallbacks, AdImage.Listener, AdPlayer.Listener, FullScreenListener {
    private static final int AFTER_ERROR_REFRESH_RATE = 10;
    private static final int SDK_NOT_INITIALIZED_RETRY_TIME = 3;
    private AdRequest mAdRequest;
    private Activity mContext;
    AdImage mImage;
    AdPlayer mPlayer;
    private Listener mListener;
    private Ad mAd;
    private Boolean mFailedToLoad = false;
    private Boolean mAdIsReady = false;
    protected Timer mAdDisplayTimer = new Timer();
    protected Integer mCurrentAdDisplayTime = 0;
    protected Integer mTimeToRefresh = 30;
    private AdOrientation mOrientation;
    private String mUnitId;
    private AdSizeType mSize;

    private final View.OnClickListener onAdClickListener = view -> {
        if (mAd == null) {
            mContext.runOnUiThread(() -> Toast.makeText(mContext, "AD NOT LOADED YET", Toast.LENGTH_SHORT).show());
            return;
        }
        mAdRequest.sendClick(mContext, mAd);

        if (mListener != null) mContext.runOnUiThread(() -> mListener.onClicked());

    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stopAdStartedTimer();
        FullScreenEventManager.unregisterFullScreenListener(this);

        mContext.getApplication().unregisterActivityLifecycleCallbacks(AdView.this);

    }

    public AdView(Context context, String unitId, AdSizeType size, AdOrientation orientation) {
        super(context);
        this.mUnitId = unitId;
        this.mSize = size;
        this.mOrientation = orientation;
        init();
    }

    public AdView(Context context) {
        super(context);
        this.mUnitId = "";
        this.mSize = AdSizeType.BANNER;
        this.mOrientation = AdOrientation.LANDSCAPE;
        init();
    }

    public AdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAdAttributes(context, attrs);
        init();
    }

    public AdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAdAttributes(context, attrs);
        init();
    }

    public AdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAdAttributes(context, attrs);
        init();
    }

    public AdSizeType getSize() {
        return mSize;
    }

    public String getUnitId() {
        return mUnitId;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int childCount = getChildCount();
        int x = 0;
        int y = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(x, y, x + child.getMeasuredWidth(), y + child.getMeasuredHeight());
            x += child.getMeasuredWidth();
            if (i % 2 != 0) {
                x = 0;
                y += child.getMeasuredHeight();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);


        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    void getAdAttributes(Context context, AttributeSet attrs) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AdView, 0, 0);

        this.mOrientation = attributes.getInteger(R.styleable.AdView_orientation, 0) == 1 ? AdOrientation.PORTRAIT : AdOrientation.LANDSCAPE;
        this.mSize = AdSizeType.values()[(attributes.getInteger(R.styleable.AdView_size, 0))];
        this.mUnitId = attributes.getString(R.styleable.AdView_unit_id);
    }

    void init() {
        if (mUnitId == null || mUnitId.equals("")) {
            if (isInEditMode()) showErrorOnPreview();
            else throw new IllegalArgumentException("You must provide an unit_id for AdView");
        }
        // this prevent preview problems on android studio
        if (isInEditMode()) return;

        mContext = (Activity) getContext();

        FullScreenEventManager.registerFullScreenListener(this);
        setOnClickListener(onAdClickListener);
        mContext.getApplication().registerActivityLifecycleCallbacks(AdView.this);
        mAdRequest = new AdRequest(mUnitId);
        loadAd();
    }

    @SuppressLint("SetTextI18n")
    void showErrorOnPreview() {
        this.setBackgroundColor(Color.parseColor("#FF0000"));
        TextView text = new TextView(getContext());
        text.setTextColor(Color.parseColor("#FFFFFF"));
        text.setText("MISSING UNIT_ID");
        addView(text);
    }

    private void loadAd() {
        (new Thread(() -> {
            mFailedToLoad = false;
            mAdIsReady = false;

            try {
                HashMap<String, Object> options = new HashMap<>();
                options.put("orientation", mOrientation.toString());
                options.put("dimension", mSize.toString());

                mAd = mAdRequest.getAd(options);

                if (mAd.getType() != AdType.BANNER)
                    throw new AdRequestException(AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE);

                presentAd(mAd);

            } catch (AdRequestException e) {
                mFailedToLoad = true;
                if (Objects.equals(e.getCode(), AdRequestException.SDK_NOT_INITIALIZED)) {
                    mTimeToRefresh = SDK_NOT_INITIALIZED_RETRY_TIME;
                } else {
                    mTimeToRefresh = AFTER_ERROR_REFRESH_RATE;
                }
                startAdDisplayTimer();
                if (mListener != null) mContext.runOnUiThread(() -> mListener.onFailedToLoad(e));
            }

        })).start();
    }

    void presentAd(Ad ad) {

        mContext.runOnUiThread(() -> {
            mCurrentAdDisplayTime = 0;
            if (ad.getMediaType() == AdMediaType.IMAGE) {
                this.mImage = new AdImage(mContext, ad.getMediaUrl(), this);
                mImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                super.addView(mImage);
                return;
            }

            this.mPlayer = new AdPlayer(mContext, ad.getMediaUrl(), this);
            mPlayer.setScaleType(AdPlayer.ScaleType.FIT_CENTER);
            mPlayer.setMuted(true);
            super.addView(mPlayer);


        });
    }

    public void reload() {
        refreshAd();
    }

    private void refreshAd() {
        if (mAd != null) mAd = null;
        stopAdStartedTimer();
        mCurrentAdDisplayTime = 0;
        if (mImage != null) {
            removeView(mImage);
            mImage = null;
        }
        if (mPlayer != null) {
            removeView(mPlayer);
            mPlayer.release();
            mPlayer = null;
        }
        loadAd();
    }

    private void onDisplayTimeChanged(double adStartedTime) {
        if (adStartedTime >= mTimeToRefresh) {
            refreshAd();
        }
    }

    private void startAdDisplayTimer() {
        if (mAdDisplayTimer != null) mAdDisplayTimer.cancel();
        if (Objects.equals(mTimeToRefresh, Ad.DISABLED_REFRESH_RATE)) return;

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

    private void stopAdStartedTimer() {
        if (mAdDisplayTimer != null) {
            mAdDisplayTimer.cancel();
            mAdDisplayTimer = null;
        }
    }

    private Integer getAdDisplayTime(Integer refreshRate) {
        if (refreshRate == Ad.AUTO_REFRESH_RATE) {
            if (mAd.getMediaType() == AdMediaType.VIDEO) {
                return mPlayer.getAdDuration();
            }
            if (mAd.getMediaType() == AdMediaType.IMAGE) {
                return Ad.DEFAULT_REFRESH_RATE;
            }

        }

        // 0 or 30-150
        return refreshRate;

    }

    @Override
    public void onImageReady() {
        mAdIsReady = true;
        mTimeToRefresh = getAdDisplayTime(mAd.getRefreshRate());

        if (mListener != null) {
            mContext.runOnUiThread(() -> {
                mListener.onLoad(this);
                mListener.onImpression();
            });
        }

        startAdDisplayTimer();
        mAdRequest.sendImpression(mContext, mAd);
    }

    @Override
    public void onImageError() {
        mFailedToLoad = true;
        mTimeToRefresh = AFTER_ERROR_REFRESH_RATE;

        startAdDisplayTimer();

        if (mListener != null)
            mContext.runOnUiThread(() -> mListener.onFailedToLoad(new AdRequestException(AdRequestException.NETWORK_ERROR)));
    }

    public boolean isLoaded() {
        return this.mAdIsReady;
    }

    public boolean isFailed() {
        return this.mFailedToLoad;
    }

    public AdOrientation getOrientation() {
        return mOrientation;
    }

    void pauseAd() {
        stopAdStartedTimer();
        if (mPlayer != null && mAd.getMediaType() == AdMediaType.VIDEO) mPlayer.pause();
    }

    void resumeAd() {
        startAdDisplayTimer();
        if (mPlayer != null && mAd.getMediaType() == AdMediaType.VIDEO) mPlayer.play();
    }

    @Override
    public void onVideoProgressChanged(double position, double total) {
        mCurrentAdDisplayTime = (int) position;
    }

    private boolean isCurrentActivity(Activity activity) {
        return mContext.equals(activity);
    }

    @Override
    public void onVideoReady(int videoDuration) {
        mTimeToRefresh = getAdDisplayTime(mAd.getRefreshRate());

        if (mListener != null) {
            mContext.runOnUiThread(() -> {
                mListener.onLoad(this);
                mListener.onImpression();
            });
        }

        mPlayer.play();
        startAdDisplayTimer();
        mAdRequest.sendImpression(mContext, mAd);
    }

    @Override
    public void onVideoFinished() {
        refreshAd();
    }

    @Override
    public void onVideoError() {
        if (mListener != null)
            mListener.onFailedToLoad(new AdRequestException(AdRequestException.PLAYBACK_ERROR));
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (isCurrentActivity(activity)) {
            resumeAd();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (isCurrentActivity(activity)) {
            pauseAd();
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
        if (isCurrentActivity(activity)) {
            stopAdStartedTimer();
            FullScreenEventManager.unregisterFullScreenListener(this);

            if (mContext != null) {
                mContext.getApplication().unregisterActivityLifecycleCallbacks(AdView.this);
            }

            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        }
    }

    @Override
    public void onFullScreenShown(int ignored) {
        pauseAd();
    }

    @Override
    public void onFullScreenDismissed() {
        resumeAd();
    }

    public interface Listener extends BaseAdListener<AdView> {
    }

}
